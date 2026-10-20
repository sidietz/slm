# Architectural Plan: Vehicle Refuelings, Station & Charging Point Hierarchy, and Spritmonitor CSV Import (Refined)

## 1. Context & Objectives

In **Simon's Life Manager (SLM)**, the vehicles section currently tracks vehicle specifications ([`Car`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Car.java)), fleet valuation, and trip logs ([`CarTrip`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarTrip.java)). However, there is no functionality for tracking vehicle refuelings, fuel costs, price-per-liter trends, or fuel economy (L/100km).

Many vehicle owners log refuelings using **Spritmonitor.de**. The project includes a reference export [`1676970_fuelings.csv`](file:///home/simon/code/eclipse/slm/1676970_fuelings.csv), containing German semicolon-separated refueling data with odometer readings, trip distances, fuel quantities, costs, tire choices, and gas stations.

### Primary Goals:
1. **Manual Refueling Tracking**: Add refueling logs with date/time, odometer (km), trip distance (km), fuel quantity (liters), cost per liter (€/L), total cost (€), station foreign key, fuel type, tank type (Full/Partial), and notes.
2. **Interactive Billboard.js Chart**: Sleek, interactive time-series chart switchable between:
   - **Consumption per 100km** (L/100km)
   - **Driven Distance** (Trip km)
   - **Price per Liter** (€/L)
3. **Native UUIDv7 Primary Keys**: All new entities (`Station`, `GasStation`, `ChargingStation`, `ChargingPoint`, `Refueling`) adopt native `UUIDv7` primary keys (`id UUID PRIMARY KEY DEFAULT uuidv7()`), adhering to repository standard ADR 018.
4. **Vehicle-Scoped Spritmonitor CSV Import**: Spritmonitor exports **one CSV per tracked vehicle** (no vehicle ID column inside the CSV). In SLM, CSV import is explicitly vehicle-scoped: the user selects the vehicle from a dropdown in the import modal, or imports directly while viewing a specific vehicle (`/refuelings?carId=...` or `/cars`), ensuring the target vehicle is always unambiguously known.
5. **Polymorphic Station Hierarchy**: Implement joined-table inheritance (`InheritanceType.JOINED`) for `Station` with four station types: `GAS_STATION`, `CHARGING_STATION`, `H2_STATION`, and `OTHER`.
6. **TankenTanken / Tankerkönig Integration Ready**: `GasStation` links directly to real-world gas station databases via `tankentanken_id UUID UNIQUE` (e.g. `9123b96c-a68e-485e-b5e6-3095d42d55bc`).
7. **Heterogeneous EV Charging Architecture (`charging_point`)**: Model `ChargingStation` (1:N) with `ChargingPoint` children to represent stations containing stalls with diverse power outputs (e.g. 300 kW HPC vs 150 kW DC vs 22 kW AC) and plug standards.
8. **Alphabetical `plug_type` Enum**: `CCS_TYPE2`, `CHADEMO`, `GBT`, `OTHER`, `TESLA`, `TYPE_1`, `TYPE_2`, `UNKNOWN` (with UI `<optgroup>` grouping for European relevance).
9. **Strict International Standardization**:
   - **ISO 3166-1 alpha-2 Country Codes**: `VARCHAR(2)` with regex check constraint (`CHECK (country ~ '^[A-Z]{2}$')`).
   - **ISO 4217 Currency Codes**: `VARCHAR(3)` with regex check constraint (`CHECK (currency ~ '^[A-Z]{3}$')`).
10. **Auto-Calculations & BC Telemetry**: Dynamic two-way calculation of `costPerLiter = totalCost / liters`, physical consumption, and extraction of Board Computer (BC / *Bordcomputer*) telemetry.
11. **Vehicles Hub Integration & Spring AI MCP Tools**: Interactive UI at `/refuelings` and `/stations`, alongside MCP tools for external AI agents.

---

## 2. Spritmonitor CSV Specification & "BC" Telemetry Analysis

Based on [`1676970_fuelings.csv`](file:///home/simon/code/eclipse/slm/1676970_fuelings.csv):
```csv
Datum;Km-Stand;Teil-Km;Spritmenge;Kosten;Währung;Tankart;Reifen;Strecken;Fahrweise;Kraftstoff;Bemerkung;Verbrauch;BC-Verbrauch;BC-Spritmenge;BC-Geschwindigkeit;Tankstelle;Land;Großraum;Ort;Breitengrad;Längengrad
09.09.2026;69982,00;601,00;38,49;85,41;"EUR";1;2;14;2;7;"";6,40;0,00;0,00;0,00;"Orlen";"D";"";"";;
29.06.2026;69381,00;453,00;28,73;52,83;"EUR";1;2;14;2;7;"";6,34;0,00;0,00;0,00;"Orlen";"D";"";"Ansbach";;
28.10.2025;65973,00;;15,93;25,95;"EUR";3;2;12;2;7;"";;0,00;0,00;0,00;"Orlen";"D";"";"";;
```

### 2.1. Spritmonitor Vehicle Isolation & Import Workflow
- In Spritmonitor, each export file corresponds to a **single tracked vehicle**.
- The CSV file contains **no vehicle identifier column**.
- **Design Decision**:
  - The `car` table and [`Car.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Car.java) remain completely unmodified.
  - CSV import is strictly vehicle-scoped:
    1. **Import Modal Dropdown**: The import dialog features a required vehicle selector dropdown populated with the user's fleet.
    2. **Context-Aware Pre-selection**: When the user opens the modal from a filtered vehicle tab (e.g. `/refuelings?carId=1`) or navigates from a vehicle page, that vehicle is automatically pre-selected.
    3. **Controller Signature**: `POST /refuelings/import` requires `@RequestParam("file") MultipartFile file` and `@RequestParam("carId") Long carId`.

### 2.2. What does the "BC" abbreviation stand for?
In automotive engineering and Spritmonitor telemetry exports, **BC** stands for **Board Computer** (German: ***Bordcomputer***, also commonly termed in automotive manuals as *On-Board Computer (OBC)*, *Trip Computer*, or *Multi-Function Display (MFD)*).

- **`BC-Verbrauch`** $\rightarrow$ **Board Computer Fuel Consumption** (*Bordcomputer-Verbrauch*, in **L/100km**): Internally estimated fuel economy computed by the Engine Control Unit (ECU) from injector pulse-width and wheel distance.
- **`BC-Spritmenge`** $\rightarrow$ **Board Computer Fuel Quantity** (*Bordcomputer-Spritmenge*, in **Liters**): Integrated fuel volume metered by ECU across the trip interval.
- **`BC-Geschwindigkeit`** $\rightarrow$ **Board Computer Average Speed** (*Bordcomputer-Geschwindigkeit*, in **km/h**): Average vehicle velocity measured across the trip interval.

| Spritmonitor CSV Header | Full German Name | Full English Automotive Name | Unit | SLM Database Column Name | Origin / Sensing Method |
|---|---|---|---|---|---|
| `Verbrauch` | *Berechneter Verbrauch* | Physical Calculated Consumption | L/100km | `calculated_consumption` | Mathematical ground truth: $(\text{liters} \times 100) / \text{trip\_distance}$ based on gas pump receipt |
| `BC-Verbrauch` | *Bordcomputer-Verbrauch* | Board Computer Consumption | L/100km | `board_computer_consumption` | ECU telemetry derived from fuel injector pulse-width & wheel speed sensors |
| `BC-Spritmenge` | *Bordcomputer-Spritmenge* | Board Computer Fuel Quantity | L | `board_computer_quantity` | ECU integrated injector fuel flow volume across the trip meter |
| `BC-Geschwindigkeit` | *Bordcomputer-Geschwindigkeit* | Board Computer Average Speed | km/h | `board_computer_speed` | Trip computer average vehicle speed over the interval |

---

## 3. Station, Gas Station & Charging Point Architecture

### 3.1. Why add a `charging_point` table? (Evaluation)
Real-world EV charging locations (e.g. Ionity, Fastned, Tesla Supercharger, EnBW HyperHubs) are **inherently heterogeneous**:
- A single hub frequently hosts stalls of differing power classes:
  - 4x 300 kW Ultra-Fast CCS2 stalls
  - 2x 150 kW Fast CCS2 stalls
  - 1x 50 kW CCS2 / CHAdeMO combo stall
  - 2x 22 kW AC Type 2 destination chargers
- **Flaws of a flat `charging_station` table**: If `max_power_kw` is a single column on `charging_station`, the system must either pick the highest stall rating or an artificial average, discarding per-stall fidelity.
- **Benefits of a 1:N `charging_point` table**:
  1. **Granular Power Modeling**: Accurately maps stations with mixed power levels (e.g. 300 kW vs 150 kW vs 22 kW).
  2. **Connector Typing**: Models different socket configurations per point via the `plug_type` enum.
  3. **Point Identifiers**: Retains physical stall numbers (e.g. "Stall 1A", "Ladesäule 3").
  4. **Dynamic Station Aggregates**: `ChargingStation` can compute its total point count (`points.size()`) and maximum peak power dynamically (`MAX(point.maxPowerKw)`).
  5. **Future Session Precision**: When logging EV recharges, SLM can optionally record the specific `charging_point_id` used.
- **Architectural Decision**: **Adopt `charging_point` as a 1:N child entity of `charging_station`.**

### 3.2. Gas Station & TankenTanken ID
In Germany, real-time gas station and fuel price feeds (MTS-K / Tankerkönig / TankenTanken) index gas stations by a standard UUID (e.g. `9123b96c-a68e-485e-b5e6-3095d42d55bc`).
- Instead of redundant boolean columns, `gas_station` defines:
  ```sql
  CREATE TABLE gas_station (
      id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
      tankentanken_id UUID UNIQUE
  );
  ```
- This minimalistic design establishes a direct foreign identifier for live price scraping, route integration, and automated price verification.

---

## 4. Plug Type Architecture: Alphabetical ENUM & UI Visibility

### 4.1. Alphabetical ENUM Specification
The `plug_type` enum is sorted **strictly alphabetically** as requested:
1. **`CCS_TYPE2`** (Combined Charging System Combo 2 — European DC fast charging standard)
2. **`CHADEMO`** (CHAdeMO DC standard — legacy Japanese EVs such as Nissan Leaf)
3. **`GBT`** (Guobiao GB/T 20234 standard — national standard used exclusively in China)
4. **`OTHER`** (Specialized sockets, Schuko household plugs, CEE industrial sockets)
5. **`TESLA`** (Tesla proprietary connector / NACS)
6. **`TYPE_1`** (SAE J1772 — North American & Japanese single-phase AC standard)
7. **`TYPE_2`** (Mennekes IEC 62196-2 — European mandatory three-phase AC standard)
8. **`UNKNOWN`** (Unspecified or unknown connector)

### 4.2. UI Feasibility Analysis: Hiding Rare/Regional Standards (`TYPE_1`, `GBT`)
- **Context**: Under EU Directive 2014/94/EU, all public EV charging stations in the European Union are legally mandated to offer `TYPE_2` (for AC) and `CCS_TYPE2` (for DC).
  - `TYPE_1` is practically non-existent on European charging infrastructure (only encountered on grey-market US/Japanese imports via adapters).
  - `GBT` is exclusively deployed inside the People's Republic of China and cannot physically connect to European vehicles without active protocol conversion boxes.
  - Displaying `TYPE_1` and `GBT` alongside primary European plugs in daily workflows adds unnecessary clutter.
- **Architectural Solution: Semantic `<optgroup>`**:
  Structure the HTML `<select>` dropdown into clean semantic groups:
  ```html
  <select name="plugType" class="form-select">
      <optgroup label="European Standards">
          <option value="CCS_TYPE2">CCS Type 2</option>
          <option value="TYPE_2">Type 2 (Mennekes)</option>
          <option value="TESLA">Tesla</option>
          <option value="CHADEMO">CHAdeMO</option>
      </optgroup>
      <optgroup label="International / Rare Standards">
          <option value="TYPE_1">Type 1 (J1772)</option>
          <option value="GBT">GB/T (China)</option>
          <option value="OTHER">Other</option>
          <option value="UNKNOWN">Unknown</option>
      </optgroup>
  </select>
  ```
  *Advantage*: Zero JavaScript needed, accessible, fast, and does not restrict the user if they actually encounter an exotic or imported vehicle.

---

## 5. International Standards Architecture

### 5.1. Currency: ISO 4217 (`VARCHAR(3)`)
- Defined as `currency VARCHAR(3) DEFAULT 'EUR' CHECK (currency ~ '^[A-Z]{3}$')`.
- Validated via `java.util.Currency.getInstance(currency)`.

### 5.2. Country: ISO 3166-1 alpha-2 (`VARCHAR(2)`)
- Defined as `country VARCHAR(2) CHECK (country ~ '^[A-Z]{2}$')`.
- Validated in Java via `java.util.Locale.getISOCountries()`.
- Spritmonitor CSV import provides an automated nationality plate translation dictionary:
  `"D"` $\rightarrow$ `"DE"`, `"A"` $\rightarrow$ `"AT"`, `"CH"` $\rightarrow$ `"CH"`, `"F"` $\rightarrow$ `"FR"`, `"I"` $\rightarrow$ `"IT"`, `"PL"` $\rightarrow$ `"PL"`, `"CZ"` $\rightarrow$ `"CZ"`, `"NL"` $\rightarrow$ `"NL"`, `"BE"` $\rightarrow$ `"BE"`, `"LU"` $\rightarrow$ `"LU"`, `"DK"` $\rightarrow$ `"DK"`.

---

## 6. Database Schema & Migration Architecture

### 6.1. Enums: `enums.sql`
```sql
CREATE TYPE fuel_type AS ENUM (
    'DIESEL',
    'SUPER_E10',
    'SUPER_E5',
    'SUPER_PLUS',
    'PREMIUM_DIESEL',
    'LPG',
    'CNG',
    'ELECTRIC',
    'HYDROGEN',
    'OTHER'
);

CREATE TYPE fueling_type AS ENUM (
    'FULL',
    'PARTIAL',
    'INITIAL'
);

CREATE TYPE driving_style AS ENUM (
    'ECONOMICAL',
    'NORMAL',
    'FAST'
);

CREATE TYPE tire_type AS ENUM (
    'SUMMER',
    'WINTER',
    'ALL_SEASON'
);

CREATE TYPE station_type AS ENUM (
    'GAS_STATION',
    'CHARGING_STATION',
    'H2_STATION',
    'OTHER'
);

CREATE TYPE plug_type AS ENUM (
    'CCS_TYPE2',
    'CHADEMO',
    'GBT',
    'OTHER',
    'TESLA',
    'TYPE_1',
    'TYPE_2',
    'UNKNOWN'
);
```

### 6.2. Database Tables: `tables.sql`

```sql
-- Polymorphic Base Station (Native UUIDv7)
CREATE TABLE station (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name TEXT NOT NULL,
    brand TEXT,
    station_type station_type NOT NULL,
    street TEXT,
    house_number TEXT,
    postal_code TEXT,
    city TEXT,
    country VARCHAR(2) CHECK (country ~ '^[A-Z]{2}$'),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    point_id BIGINT REFERENCES point(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_station_name ON station(name);
CREATE INDEX idx_station_country_city ON station(country, city);

-- Gas Station (Combustion fuels with TankenTanken UUID)
CREATE TABLE gas_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    tankentanken_id UUID UNIQUE
);
CREATE INDEX idx_gas_station_tankentanken ON gas_station(tankentanken_id);

-- Charging Station (Electric vehicles)
CREATE TABLE charging_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    operator TEXT
);

-- Charging Point (1:N under Charging Station with individual power ratings)
CREATE TABLE charging_point (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    charging_station_id UUID NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    point_identifier TEXT,
    max_power_kw NUMERIC(6, 1) NOT NULL,
    plug_type plug_type NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_charging_point_station ON charging_point(charging_station_id);

-- Vehicle Refueling Event (Native UUIDv7)
CREATE TABLE refueling (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
    station_id UUID REFERENCES station(id) ON DELETE SET NULL,
    charging_point_id UUID REFERENCES charging_point(id) ON DELETE SET NULL,
    fueling_date TIMESTAMP NOT NULL,
    odometer NUMERIC(10, 2),
    trip_distance NUMERIC(10, 2),
    liters NUMERIC(8, 2) NOT NULL,
    cost_per_liter NUMERIC(8, 3),
    total_cost NUMERIC(8, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR' CHECK (currency ~ '^[A-Z]{3}$'),
    fuel_type fuel_type,
    fueling_type fueling_type DEFAULT 'FULL',
    driving_style driving_style,
    tire_type tire_type,
    country VARCHAR(2) CHECK (country ~ '^[A-Z]{2}$'),
    location TEXT,
    routes TEXT,
    -- Calculated physical consumption from pump receipt: (liters * 100) / trip_distance
    calculated_consumption NUMERIC(6, 2),
    -- BC = Bordcomputer (Board Computer / On-Board Trip Computer telemetry from vehicle dashboard)
    board_computer_consumption NUMERIC(6, 2), -- BC-Verbrauch (L/100km)
    board_computer_quantity NUMERIC(8, 2),    -- BC-Spritmenge (Liters)
    board_computer_speed NUMERIC(6, 2),       -- BC-Geschwindigkeit (km/h)
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refueling_car_date ON refueling(car_id, fueling_date DESC);
CREATE INDEX idx_refueling_station ON refueling(station_id);
```

### 6.3. Schema Synchronization & Migration Script
Per **AGENTS.md** protocol:
1. Baseline schemas: [`src/main/resources/enums.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/enums.sql), [`src/main/resources/tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql).
2. Docker Compose bootstrap: [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql).
3. Testcontainers suite schema: [`src/test/resources/schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql).
4. Atomic transactional migration script: [`src/main/resources/migration_2026-10-19_3.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-19_3.sql) enclosed in `BEGIN;` ... `COMMIT;`.

---

## 7. Domain Model & Business Logic

### 7.1. Station JPA Hierarchy ([`de.oberamsystems.slm.model`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/))
- **`PlugType`**:
  ```java
  public enum PlugType {
      CCS_TYPE2("CCS Type 2", true),
      CHADEMO("CHAdeMO", true),
      GBT("GB/T", false),
      OTHER("Other", true),
      TESLA("Tesla", true),
      TYPE_1("Type 1", false),
      TYPE_2("Type 2", true),
      UNKNOWN("Unknown", true);

      private final String displayName;
      private final boolean commonInEurope;

      PlugType(String displayName, boolean commonInEurope) {
          this.displayName = displayName;
          this.commonInEurope = commonInEurope;
      }

      public String getDisplayName() { return displayName; }
      public boolean isCommonInEurope() { return commonInEurope; }
  }
  ```
- **`Station`**: Base joined entity with native UUIDv7.
- **`GasStation`**: Joined entity with `tankentankenId UUID`.
- **`ChargingStation`**: Joined entity with `List<ChargingPoint> chargingPoints`.
- **`ChargingPoint`**:
  ```java
  @Entity
  @Table(name = "charging_point")
  public class ChargingPoint {
      @Id
      @GeneratedValue
      @UuidGenerator(style = Style.VERSION_7)
      @Column(name = "id", columnDefinition = "uuid")
      private UUID id;

      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "charging_station_id", nullable = false)
      private ChargingStation chargingStation;

      @Column(name = "point_identifier")
      private String pointIdentifier;

      @Column(name = "max_power_kw", precision = 6, scale = 1, nullable = false)
      private BigDecimal maxPowerKw;

      @Enumerated(EnumType.STRING)
      @JdbcType(PostgreSQLEnumJdbcType.class)
      @Column(name = "plug_type", nullable = false)
      private PlugType plugType;
  }
  ```

### 7.2. JPA Entity: `Refueling` ([`de.oberamsystems.slm.model.Refueling`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/))
- Native UUIDv7 primary key.
- ManyToOne relationship to `Car`, `Station`, and optional `ChargingPoint`.
- ISO 3166-1 alpha-2 `country` and ISO 4217 `currency`.
- Board Computer telemetry fields (`boardComputerConsumption`, `boardComputerQuantity`, `boardComputerSpeed`).

### 7.3. Repositories
- `StationRepository`: `findAllByOrderByNameAsc()`.
- `GasStationRepository`: `findAllByOrderByNameAsc()`, `findByNameIgnoreCaseAndCityIgnoreCase(String name, String city)`, `findByTankentankenId(UUID id)`.
- `ChargingStationRepository`: `findAllByOrderByNameAsc()`.
- `ChargingPointRepository`: `findByChargingStationIdOrderByMaxPowerKwDesc(UUID stationId)`.
- `RefuelingRepository`: `findAllByOrderByFuelingDateDesc()`, `findByCarIdOrderByFuelingDateDesc(Long carId)`, `findAllByOrderByFuelingDateAsc()`, `findByCarIdOrderByFuelingDateAsc(Long carId)`.

### 7.4. Service: `SpritmonitorCsvService` ([`de.oberamsystems.slm.service.SpritmonitorCsvService`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/service/))
- Parses semicolon CSV for the selected `targetCar`.
- Translates national plate codes (`"D"` $\rightarrow$ `"DE"`).
- Resolves/provisions `GasStation` by name and city.
- Converts decimal commas and calculates missing unit costs.
- Maps `BC-*` telemetry readings.

---

## 8. Web UI, Interactive Billboard.js Chart & Controller Architecture

### 8.1. Switchable Billboard.js Analytics Chart (`#refuelingChart`)
Mounted prominently on `/refuelings` inside a sleek card above the logs table, powered by **Billboard.js / D3**:
- **Metric Switcher (Pill/Button Group)**:
  1. **`Consumption per 100km`** (Default):
     - Displays actual physical consumption (`calculatedConsumption` in L/100km) as a primary green/theme line.
     - Where recorded, also displays board computer consumption (`boardComputerConsumption` in L/100km) as a dashed comparison line, allowing the driver to visually inspect the "BC Delta".
  2. **`Driven Distance`** (Trip Distance in km):
     - Displays driven kilometers per tank interval (`tripDistance`) as a bar or area-spline series in vibrant blue.
  3. **`Price per Liter`** (€/L):
     - Displays fuel price development (`costPerLiter`) over time as an orange spline chart.
- **Timeseries Configuration**:
  - `axis.x`: timeseries with `tick: { format: "%d.%m.%Y" }`.
  - Tooltips with localized dates, values, gas station name, and vehicle name.
  - Smooth animation on switching metrics: `chart.load({ unload: true, columns: [...] })`.
- **Reactive Vehicle Filtering**:
  - When switching vehicle tabs ("All Vehicles" vs specific car), the chart dynamically re-loads the filtered chronological series.

### 8.2. REST API Endpoint: `GET /api/refuelings`
To support asynchronous chart updates without full page reloads:
- `GET /api/refuelings?carId={carId}`: Returns JSON array of refueling records sorted chronologically ascending (`fuelingDate ASC`) with formatted date, `calculatedConsumption`, `boardComputerConsumption`, `tripDistance`, `costPerLiter`, `stationName`, and `carName`.

### 8.3. Controller: `RefuelingController` ([`de.oberamsystems.slm.controller.RefuelingController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/))
- **`GET /refuelings`**: Filter by vehicle via `carId`, prepare KPIs, stations list, enums, and initial chart dataset.
- **`GET /api/refuelings`**: JSON REST endpoint for Billboard.js chart.
- **`POST /refuelings`**: Save manual refueling entry.
- **`POST /refuelings/import`**: Accepts multipart CSV upload and target `carId`.
- **`POST /refuelings/{id}/delete`**: Delete refueling entry.

### 8.4. Controller: `StationController` ([`de.oberamsystems.slm.controller.StationController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/))
- **`GET /stations`**: Display Gas Stations and EV Charging Stations.
- **`POST /stations/gas`**: Create Gas Station with optional TankenTanken UUID.
- **`POST /stations/charging`**: Create Charging Station with Charging Points using `<optgroup>` plug types.
- **`POST /stations/{id}/delete`**: Delete Station.

### 8.5. Views
- **`refuelings.html`**:
  - Hero banner with `bi bi-fuel-pump`.
  - Quick Nav: Vehicles, Refuelings, Stations, Trips, Routes, Points.
  - Vehicle Filter Tabs & Summary KPI row.
  - **Billboard.js Analytics Chart Card**: With interactive switcher buttons (Consumption, Distance, Price/L).
  - Refuelings Table with station, ISO country badge, consumption badge, BC values.
  - Add Refueling Modal & Spritmonitor CSV Import Modal (pre-selected to active vehicle).
- **`stations.html`**:
  - Dual tabs for Gas Stations and Charging Stations (listing charging point stalls, max power kW, and plug type badges).

---

## 9. Spring AI Model Context Protocol (MCP) Server Tools

Create [`McpVehicleTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java) registered in [`McpServerConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpServerConfig.java):
1. **`getRefuelings(carId, limit)`**: Query refueling history with station details, volume, price, and consumption.
2. **`logRefueling(...)`**: Record a new refueling entry.
3. **`getStations(stationType, country)`**: List gas and charging stations.
4. **`createStation(...)`**: Provision a new station (gas or charging).

---

## 10. Verification & Automated Test Plan

1. **Unit Tests**:
   - `SpritmonitorCsvServiceTest`: Verifies parsing of `1676970_fuelings.csv`, ISO 3166 country conversion (`"D"` $\rightarrow$ `"DE"`), `GasStation` auto-provisioning, decimal conversions, Board Computer telemetry extraction, and cost-per-liter calculation.
2. **Integration Tests**:
   - `RefuelingIT` (extending `AbstractIntegrationTest` with Testcontainers):
     - Test native UUIDv7 persistence across `Refueling`, `Station`, `GasStation`, `ChargingStation`, and `ChargingPoint`.
     - Test `PlugType` enum persistence and querying.
     - Test manual refueling creation linked to a `GasStation`.
     - Test multipart CSV upload importing refuelings with explicit `carId`.
     - Test `GET /api/refuelings` returning chronologically sorted JSON data for Billboard.js chart.
     - Test foreign key integrity: deleting a station sets `station_id = NULL` on refuelings (`ON DELETE SET NULL`).
     - Test `charging_point` cascade deletion on `charging_station`.
3. **Regression Tests**:
   - Run `./gradlew test -x jacocoTestReport -x compileAotTestJava`.

---

## 11. Documentation Updates

1. **`DECISIONS.md`**: Add ADR 022 documenting the Refueling schema with UUIDv7, polymorphic Station hierarchy (`Station`, `GasStation`, `ChargingStation`), `ChargingPoint` 1:N composition, `PlugType` alphabetical enum with UI grouping, Billboard.js chart switcher, `tankentanken_id`, ISO 3166 country validation, ISO 4217 currency enforcement, and Board Computer telemetry fields.
2. **`CHANGELOG.md`**: Add release entry under `## [Unreleased]`.
3. **`README.md`**: Update Section 5 (Logistics, Spatial GIS & Travel) and Section 8 (MCP Server).
