# Plan: Car-Related Expense Tracking, Spritmonitor Taxonomy & Lifecycle TCO Analytics (€/100km)

## 1. Problem Statement & User Need

In Simon's Life Manager (SLM), vehicle tracking currently covers fuel consumption and distance via `Refueling` events ([`refuelings.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/refuelings.html)) and fleet inventory via `Car` ([`cars.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/cars.html)). However:
1. **Lack of Operating Expense Tracking**: Non-fuel expenses—such as insurance, taxes, regular maintenance, repairs, inspections (TÜV/HU/AU), parking fees, tolls, digital subscriptions, and EV-specific charging base fees or GHG bonuses (THG-Prämie)—cannot currently be logged or categorized.
2. **Missing Lifecycle Depreciation Parameters**: `Car` tracks `purchasePrice`, `sellPrice`, `purchaseKilometers`, and `sellKilometers`, but lacks a planned `targetSellDate` and expected `residualValue` for vehicles still in active ownership.
3. **Absence of Real-World Cost Analytics (€/100km)**: The field `price_per_100_km` on `Car` is a static manual number. Users need dynamic **Total Cost of Ownership (TCO)** and automated **Price per 100 km (€/100km)** analytics integrating:
   - Depreciation ($\text{Purchase Price} - \text{Residual Value}$)
   - Fuel and charging expenses ($\sum \text{Refuelings}$)
   - Operating costs ($\sum \text{Expenses}$ across categories)
   - Realized distance driven to date vs. projected lifecycle distance at the target sell date.

---

## 2. Spritmonitor.de Expense Taxonomy Mapping

We will implement all 23 Spritmonitor.de expense categories into a dedicated Java enum `CarExpenseType` and PostgreSQL enum `car_expense_type` using clean, idiomatic **English enum identifiers** with German and English labels, Bootstrap icons, badge CSS styling, and credit/debit sign semantics:

| ID | English Enum Constant | English Display Label | Spritmonitor Name (DE) | Icon | Type |
|---|---|---|---|---|---|
| **1** | `MAINTENANCE` | Maintenance | Wartung | `bi-wrench-adjustable` | Expense |
| **2** | `REPAIR` | Repair | Reparatur | `bi-tools` | Expense |
| **3** | `TIRE_CHANGE` | Tire Change | Reifenwechsel | `bi-disc` | Expense |
| **4** | `OIL_CHANGE` | Oil Change | Ölwechsel | `bi-droplet-half` | Expense |
| **5** | `INSURANCE` | Insurance | Versicherung | `bi-shield-check` | Expense |
| **6** | `TAX` | Tax | Steuer | `bi-receipt` | Expense |
| **7** | `INSPECTION` | Inspection / TÜV | HU/AU (TÜV) | `bi-clipboard-check` | Expense |
| **8** | `TUNING` | Tuning | Tuning | `bi-speedometer` | Expense |
| **9** | `ACCESSORIES` | Accessories | Zubehör | `bi-box-seam` | Expense |
| **10** | `PURCHASE_PRICE` | Purchase Price | Kaufpreis | `bi-cash-stack` | Capital |
| **11** | `OTHER` | Other | Sonstiges | `bi-three-dots` | Expense |
| **12** | `CARE` | Care & Wash | Wagenpflege | `bi-stars` | Expense |
| **13** | `LEASING_RATE` | Leasing Rate | Leasingrate | `bi-calendar-range` | Recurring |
| **14** | `REGISTRATION` | Registration | Zulassung/Ummeldung | `bi-card-heading` | Expense |
| **15** | `FINANCING_RATE` | Financing Rate | Finanzierungsrate | `bi-bank` | Recurring |
| **16** | `REFUND` | Refund / Reimbursement | Erstattung | `bi-arrow-down-left-circle` | **Credit (Inflow)** |
| **17** | `FINE` | Traffic Fine | Bußgeld | `bi-exclamation-octagon` | Expense |
| **18** | `PARKING_FEE` | Parking Fee | Parkgebühr | `bi-p-circle` | Expense |
| **19** | `TOLL` | Toll | Maut | `bi-signpost` | Expense |
| **20** | `SPARE_PARTS` | Spare Parts | Ersatzteile | `bi-gear` | Expense |
| **21** | `CHARGING_BASE_FEE` | Charging Base Fee | Ladegrundgebühr | `bi-lightning-charge` | Recurring |
| **22** | `DIGITAL_SERVICES` | Digital Services | Digitale Dienste | `bi-wifi` | Recurring |
| **23** | `GHG_QUOTA` | GHG Quota (Bonus) | THG-Prämie | `bi-tree` | **Credit (Inflow)** |

> [!NOTE]
> `REFUND` (ID 16, Erstattung) and `GHG_QUOTA` (ID 23, THG-Prämie) represent **credits / inflows** that reduce the total cost of ownership. The system cleanly handles positive numeric entries from the user while automatically subtracting them in net financial analytics.

---

## 3. Financial Analytics & €/100km Mathematical Model

The Expense Analytics Tab will compute the **Total Cost of Ownership (TCO)** and **Price per 100 km (€/100km)** through two complementary lenses:

```mermaid
graph TD
    subgraph Total Cost of Ownership (TCO)
        DEP[Depreciation: Purchase Price - Residual Value]
        FUEL[Fuel & Charging Costs: Sum of Refuelings]
        EXP[Operating Expenses: Repairs, Insurance, Taxes, etc.]
        CRED[Credits & Inflows: THG-Prämie, Erstattungen]
        TCO[Net TCO = DEP + FUEL + EXP - CRED]
        DEP --> TCO
        FUEL --> TCO
        EXP --> TCO
        CRED -. Subtracted .-> TCO
    end

    subgraph Mileage Tracking
        P_KM[Purchase Mileage: car.purchaseKilometers]
        CUR_KM[Current Mileage: max(Refueling.odometer, CarExpense.odometer)]
        DIST[Distance Driven to Date: CUR_KM - P_KM]
        P_KM --> DIST
        CUR_KM --> DIST
    end

    subgraph Cost per 100km
        TCO --> P100[Price per 100km = (Net TCO / Distance Driven) * 100]
        DIST --> P100
    end
```

### 1. Realized Metrics (To Date)
- **Ownership Duration**: Days elapsed since `car.purchaseDate`.
- **Distance Driven**: $\Delta km = \text{Current Odometer} - \text{car.purchaseKilometers}$.
- **Net Operating Expenses**: $\sum \text{CarExpense (debits)} - \sum \text{CarExpense (credits)}$.
- **Total Fuel Costs**: $\sum \text{Refueling.totalCost}$.
- **Realized Depreciation**:
  - If vehicle is sold (`car.sellPrice > 0`): $\text{purchasePrice} - \text{sellPrice}$.
  - If vehicle is active: $\text{purchasePrice} - \text{residualValue}$ (or linearly pro-rated over time to `targetSellDate`).
- **Realized Price per 100 km**:
  $$\text{Price / 100 km} = \frac{\text{Realized TCO}}{\Delta km} \times 100$$
- **Component Breakdown (€/100km)**:
  - Depreciation share: $(\text{Depreciation} / \Delta km) \times 100$
  - Fuel share: $(\text{Fuel Cost} / \Delta km) \times 100$
  - Fixed & Maintenance share: $(\text{Operating Expenses} / \Delta km) \times 100$

### 2. Projected Lifecycle Metrics (At `targetSellDate`)
- **Daily Mileage Rate**: $\text{km/day} = \frac{\Delta km}{\text{Days Elapsed}}$.
- **Remaining Days**: $\text{targetSellDate} - \text{Today}$.
- **Projected Final Mileage**: $\text{Current Odometer} + (\text{km/day} \times \text{Remaining Days})$.
- **Projected Lifecycle €/100km**: Projected total costs divided by projected total distance $\times 100$.

### 3. Multi-Vehicle Cost Comparison Model (Tab 3: Comparison)
The Comparison Tab allows a head-to-head comparison between two cars (e.g. Car A vs Car B, or an existing ICE car vs a prospective EV):
- **Line Chart Visualization**:
  - **X-axis**: Ownership Horizon in Years ($t = 1, 2, \dots, 10$).
  - **Y-axis**: Cumulative Total Cost in Euros (€).
  - **Interactive Parameter**: **Annual Distance (km)**, dynamically editable via an input field / slider directly in the visualization (default value: **10,000 km**; quick presets for 5,000 km, 10,000 km, 15,000 km, 20,000 km).
- **Cost Calculation Model (Taking ALL Costs into Account)**:
  For each vehicle $i \in \{A, B\}$ at year $t$ with chosen annual distance $D_{\text{annual}}$:
  $$\text{Total Cost}_i(t) = \text{Depreciation}_i(t) + \left[ \text{Variable Cost / km}_i \times D_{\text{annual}} \times t \right] + \left[ \text{Fixed Annual Costs}_i \times t \right] - \left[ \text{Annual Credits}_i \times t \right]$$
  1. **Depreciation Component $\text{Depreciation}_i(t)$**:
     - Net capital loss over ownership horizon $T_{\text{horizon}}$ (derived from `purchaseDate` to `targetSellDate`, or default 5 years):
       $$\text{Total Depreciation} = \text{purchasePrice} - \text{residualValue}$$
       $$\text{Depreciation}_i(t) = \min\left(t, T_{\text{horizon}}\right) \times \frac{\text{purchasePrice} - \text{residualValue}}{T_{\text{horizon}}}$$
       *(Takes into account both purchase price and residual value accurately across the timeframe).*
  2. **Variable / Operating Costs per km ($\text{Variable Cost / km}_i$)**:
     - Fuel / Electricity consumption per km (from actual refueling data $\sum \text{FuelCost} / \Delta km$, or `car.consumption` $\times$ fuel price).
     - Running maintenance, tires, oil, and repairs per km ($\sum \text{Variable Expenses} / \Delta km$).
  3. **Fixed Annual Operating Costs ($\text{Fixed Annual Costs}_i$)**:
     - Annual insurance (`VERSICHERUNG`), vehicle tax (`STEUER`), digital subscriptions (`DIGITALE_DIENSTE`), EV charging base fees (`LADEGRUNDGEBUEHR`), inspections (`HU_AU_TUEV` pro-rated / 2 years).
  4. **Annual Credits ($\text{Annual Credits}_i$)**:
     - Inflows reducing TCO, e.g. annual GHG bonus (`THG_PRAEMIE`) or expected reimbursements.
- **Break-Even Analysis**:
  - When comparing vehicles with differing fixed vs variable cost profiles (e.g. high purchase price / low running cost EV vs lower purchase price / higher running cost ICE), the visualization detects and highlights the **Break-Even Year** and **Break-Even Mileage** where the lines intersect.

---

## 4. Implementation Phases

### Phase 1: Database Schema & Migration Script
1. Create transactional migration [`src/main/resources/migration_2026-10-23_2.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/) enclosed in `BEGIN; ... COMMIT;`:
   - Add columns to table `car`:
     ```sql
     ALTER TABLE car ADD COLUMN IF NOT EXISTS target_sell_date DATE;
     ALTER TABLE car ADD COLUMN IF NOT EXISTS residual_value NUMERIC(10, 2);
     ```
   - Create enum `car_expense_type` with 23 English values matching Spritmonitor:
     ```sql
     CREATE TYPE car_expense_type AS ENUM (
         'MAINTENANCE',
         'REPAIR',
         'TIRE_CHANGE',
         'OIL_CHANGE',
         'INSURANCE',
         'TAX',
         'INSPECTION',
         'TUNING',
         'ACCESSORIES',
         'PURCHASE_PRICE',
         'OTHER',
         'CARE',
         'LEASING_RATE',
         'REGISTRATION',
         'FINANCING_RATE',
         'REFUND',
         'FINE',
         'PARKING_FEE',
         'TOLL',
         'SPARE_PARTS',
         'CHARGING_BASE_FEE',
         'DIGITAL_SERVICES',
         'GHG_QUOTA'
     );
     ```
   - Create table `car_expense`:
     ```sql
     CREATE TABLE car_expense (
         id UUID PRIMARY KEY DEFAULT uuidv7(),
         car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
         expense_type car_expense_type NOT NULL,
         expense_date DATE NOT NULL,
         odometer NUMERIC(10, 2),
         amount NUMERIC(10, 2) NOT NULL,
         title TEXT NOT NULL,
         note TEXT,
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     );
     CREATE INDEX idx_car_expense_car_date ON car_expense(car_id, expense_date DESC);
     CREATE INDEX idx_car_expense_type ON car_expense(expense_type);
     ```
2. Synchronize authoritative baseline files:
   - [`src/main/resources/enums.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/enums.sql)
   - [`src/main/resources/tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql)
   - [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql)
   - [`src/test/resources/schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql)

---

### Phase 2: Domain Entities & Enums
1. **Enum [`CarExpenseType.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpenseType.java)**:
   - 23 English enum constants (`MAINTENANCE`, `REPAIR`, `TIRE_CHANGE`, `OIL_CHANGE`, `INSURANCE`, `TAX`, `INSPECTION`, `TUNING`, `ACCESSORIES`, `PURCHASE_PRICE`, `OTHER`, `CARE`, `LEASING_RATE`, `REGISTRATION`, `FINANCING_RATE`, `REFUND`, `FINE`, `PARKING_FEE`, `TOLL`, `SPARE_PARTS`, `CHARGING_BASE_FEE`, `DIGITAL_SERVICES`, `GHG_QUOTA`).
   - Fields: `int spritmonitorId`, `String displayName` (English), `String germanName` (Spritmonitor DE), `String icon` (Bootstrap icon), `String badgeClass`, `boolean isCredit`.
   - Helper methods: `fromSpritmonitorId(int id)`, `isCredit()`.
2. **Entity [`CarExpense.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpense.java)**:
   - UUIDv7 primary key using `@GeneratedValue @UuidGenerator`.
   - `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "car_id", nullable = false) private Car car;`.
   - `@Enumerated(EnumType.STRING) @JdbcType(PostgreSQLEnumJdbcType.class) private CarExpenseType expenseType;`.
   - `LocalDate expenseDate`, `Long odometer`, `BigDecimal amount`, `String title`, `String note`, `LocalDateTime createdAt`.
3. **Enhance [`Car.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Car.java)**:
   - Add `private LocalDate targetSellDate;` (`@Column(name = "target_sell_date") @DateTimeFormat(pattern = "yyyy-MM-dd")`).
   - Add `private BigDecimal residualValue;` (`@Column(name = "residual_value")`).
   - Getter/setter methods, updated constructors.
4. **Repository [`CarExpenseRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpenseRepository.java)**:
   - `findByCarIdOrderByExpenseDateDesc(Long carId)`
   - `findAllByOrderByExpenseDateDesc()`
   - Aggregation queries by category.

---

### Phase 3: Controller & Analytics Service Layer
1. **Controller [`CarExpenseController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/CarExpenseController.java)**:
   - `GET /car-expenses`:
     - Parameters:
       - `Long carId` (optional, for log and analytics tabs)
       - `String tab` (`LOG`, `ANALYTICS`, or `COMPARISON`, default `LOG`)
       - `Long carAId` (for comparison tab, defaults to first car in fleet)
       - `Long carBId` (for comparison tab, defaults to second car in fleet)
       - `Integer annualDistanceKm` (for comparison tab, default `10000`)
     - Model attributes:
       - `cars`: full car fleet.
       - `selectedCar`: active car for log/analytics.
       - `expenses`: list of expenses.
       - `expenseTypes`: all 23 enum constants.
       - `kpis`: Total Expenses, Fuel Costs, Total TCO, Current €/100km, Projected €/100km.
       - `comparisonData`: computed cost coefficients for comparison cars (Car A, Car B) including depreciation, variable cost per km, fixed cost per year, annual credits, and projected multi-year trajectories.
   - `POST /car-expenses`:
     - Creates/updates expense with validation (Car, Date, Amount, Type required).
   - `POST /car-expenses/{id}/delete`:
     - Deletes expense entry and redirects.
   - `POST /car-expenses/cars/{carId}/parameters`:
     - Dedicated endpoint to quickly update `targetSellDate` and `residualValue` directly from the analytics tab without needing to navigate away to `/cars`.
2. **Update [`RouteController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/RouteController.java)**:
   - Ensure `submitCars` binds `targetSellDate` and `residualValue` when adding or editing vehicles on `/cars`.

---

### Phase 4: UI & Templates
1. **Create [`src/main/resources/templates/car-expenses.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/car-expenses.html)**:
   - Consistent SLM design language with standard header and quick navigation:
     `Vehicles | Refuelings | Expenses (active) | Stations | Trips | Routes | Points`
   - Vehicle filter bar (`All Vehicles`, `Car 1`, `Car 2`...).
   - **Triple Tab Structure**:
     - **Tab 1: Expenses Log (`#tab-expenses`)**:
       - KPI summary row (Total Operating Costs, Top Expense Category, Net Balance, Expense Entries).
       - Action buttons: "+ Add Expense", "View Analytics", "Compare Vehicles".
       - Data table: Date, Vehicle, Category (badge + icon), Title & Notes, Mileage, Amount (color-coded for debits vs credits), Actions (Edit, Delete).
       - Modal for adding and editing expenses with all 23 Spritmonitor types pre-populated.
     - **Tab 2: Expense Analytics & TCO (€/100km) (`#tab-analytics`)**:
       - Lifecycle Parameters card (Purchase Date, Purchase Km, Target Sell Date, Residual Value, Current Mileage).
       - Realized vs Projected TCO KPI summary cards.
       - Detailed breakdown table: Depreciation, Fuel, Maintenance, Insurance, Taxes, etc.
       - Prominent **Cost per 100 km Cards**:
         - **Total € / 100 km**
         - Fuel € / 100 km
         - Depreciation € / 100 km
         - Maintenance & Other € / 100 km
       - Interactive Billboard.js charts:
         - Donut chart: TCO Cost Distribution.
         - Bar chart: Monthly expenses and category shares.
     - **Tab 3: Vehicle Comparison (`#tab-comparison`)**:
       - **Interactive Comparison Header Toolbar**:
         - Dropdown for **Vehicle 1 (Car A)**.
         - Dropdown for **Vehicle 2 (Car B)**.
         - Interactive **Distance per Year Slider (noUiSlider)**:
           - Styled with **noUiSlider** (`nouislider@15.8.1` CSS & JS) connected slider with tooltips and integer formatting (range: 2,500 km to 50,000 km, step: 500 km, default: **10,000 km**).
           - Synchronized number input field allowing direct typing.
           - Quick preset pill buttons: `5,000 km`, `10,000 km` (default), `15,000 km`, `20,000 km`, `30,000 km`.
       - **Dynamic Multi-Year Line Chart (Billboard.js)**:
         - **X-axis**: Year 1, Year 2, Year 3, Year 4, Year 5, Year 6, Year 7, Year 8, Year 9, Year 10.
         - **Y-axis**: Total Cumulative Cost in Euros (€).
         - Line 1: Car A (Primary SLM Blue/Teal) showing total cost over time.
         - Line 2: Car B (Accent Coral/Amber) showing total cost over time.
         - Real-time client-side recalculation: moving the distance slider or typing in the field smoothly updates chart lines using `bb.load()` without reloading the page.
       - **Comparative Analytics Cards**:
         - Break-even status banner (e.g. *"Car A breaks even with Car B after 3.8 years at 10,000 km/year"* or *"Car B remains cheaper across all 10 years"*).
         - Side-by-side cost summary table comparing:
           - Total Cost at Year 1, 3, 5, 10.
           - Cost per km (€/km) and €/100km at the chosen annual distance.
           - Total depreciation factored into calculation.
2. **Update [`cars.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/cars.html)**:
   - Add `Target Sell Date` and `Residual Value` to the Fleet table.
   - Add inputs for `targetSellDate` and `residualValue` to `#carModal` (Add/Edit Vehicle modal).
   - Add quick action link to `/car-expenses?carId=...` for each car row.
3. **Update [`refuelings.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/refuelings.html)**:
   - Add `<a href="/car-expenses" class="btn btn-outline-secondary btn-sm"><i class="bi bi-receipt me-1"></i> Expenses</a>` to the quick navigation bar.

---

### Phase 5: MCP Tooling
In [`McpVehicleTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java):
1. `@Tool getCarExpenses`: List expenses for a car or all cars with category filtering.
2. `@Tool logCarExpense`: Log a car expense with vehicle ID, type, date, amount, odometer, title, note.
3. `@Tool getVehicleTcoAnalytics`: Get full TCO analysis and price per 100km breakdown.
4. `@Tool updateVehicleLifecycleParameters`: Update `targetSellDate` and `residualValue` for a car.

---

### Phase 6: Automated Testing & Verification
1. **Unit Tests**:
   - `CarExpenseModelTest`: Verify `CarExpenseType` values, Spritmonitor ID mapping, and credit determination.
   - `CarModelTest`: Verify `targetSellDate` and `residualValue` accessors and depreciation calculations.
2. **Integration Tests**:
   - `CarExpenseIT`:
     - Test adding, editing, and deleting expenses across different Spritmonitor types.
     - Test credit handling (`REFUND`, `GHG_QUOTA`).
     - Test updating `targetSellDate` and `residualValue` via controller endpoint.
     - Test TCO and price per 100km calculations matching expected mathematical results.
     - Test `/car-expenses?tab=COMPARISON` rendering, comparison model attributes, and multi-year cost trajectory calculations.
3. **Regression Tests**:
   - Verify `RefuelingIT` and `CourseDetailIT` pass cleanly with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test -x jacocoTestReport`.

---

### Phase 7: Documentation
1. Document **ADR 028: Car-Related Expense Tracking, Spritmonitor Taxonomy, and Lifecycle TCO Analytics (€/100km)** in [`DECISIONS.md`](file:///home/simon/code/eclipse/slm/DECISIONS.md).
2. Document all additions and changes in [`CHANGELOG.md`](file:///home/simon/code/eclipse/slm/CHANGELOG.md) under `[0.0.3-rc9]`.

---

## 5. Architectural & User Decision Points

Before proceeding with implementation, please review and confirm the following design details:
1. **Spritmonitor Types**: All 23 options provided will be supported using clean English enum constants (`MAINTENANCE`, `REPAIR`, `TIRE_CHANGE`, `OIL_CHANGE`, `INSURANCE`, `TAX`, `INSPECTION`, `TUNING`, `ACCESSORIES`, `PURCHASE_PRICE`, `OTHER`, `CARE`, `LEASING_RATE`, `REGISTRATION`, `FINANCING_RATE`, `REFUND`, `FINE`, `PARKING_FEE`, `TOLL`, `SPARE_PARTS`, `CHARGING_BASE_FEE`, `DIGITAL_SERVICES`, `GHG_QUOTA`) mapped to their Spritmonitor IDs and German labels.
2. **Credit Semantics**: `REFUND` (16, Erstattung) and `GHG_QUOTA` (23, THG-Prämie) will be treated as income/credits reducing the TCO in calculations.
3. **Depreciation Handling**: For active cars, depreciation is modeled as `purchasePrice - residualValue` across ownership horizon.
4. **UI Placement & Triple Tabs**: A dedicated `/car-expenses` hub with three tabs:
   - **Tab 1: Expenses Log**: Searchable/filterable log with + Add Expense modal.
   - **Tab 2: Expense Analytics & €/100km**: TCO cards, cost breakdown, Billboard.js donut/bar charts.
   - **Tab 3: Vehicle Comparison**: Side-by-side comparison of two cars via Billboard.js line chart (X: Year 1..10, Y: Total Cost €), editable annual mileage with default `10,000 km`, and break-even analysis.
