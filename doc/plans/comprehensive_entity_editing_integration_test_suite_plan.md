# Comprehensive Entity In-Place Editing Integration Test Suite Plan

## 1. Executive Summary & Objective

With the implementation of the **Universal Modal-First Input Architecture and In-Place Entity Editing** (ADR 024) across Simon's Life Manager (SLM), all 25+ entity views now feature full-width responsive tables and dual-purpose Bootstrap 5 modals. Entity models across all domains now expose public `setId(...)` mutators compliant with ADR 002 (JavaBean mutability) to allow Spring's `WebDataBinder` to bind existing entity IDs and execute JPA `UPDATE` statements instead of `INSERT`.

To ensure rock-solid stability and prevent regression across the entire application, this plan outlines a comprehensive integration test expansion that systematically tests in-place editing across all domains:
1. **Spend, Commercial Partners & Contracts** (`FinanceManagementIT.java`)
2. **Healthcare & Personal Entities** (`DoctorControllerIT.java`, `SocialAndPeopleIT.java`)
3. **Vehicles, Transit & Spatial Mobility** (`TransitAndMobilityIT.java`, `RefuelingIT.java`)
4. **Books, Media & Gaming Ecosystem** (`ActivitiesAndMediaIT.java`)
5. **Habits, Routines & Well-being** (`WellnessAndHabitsIT.java`, `PersonalJournalIT.java`)

---

## 2. Invariants & Testing Standard

Every edit integration test must assert the following invariants:
1. **ID Preservation & Single-Row Semantics**:
   - The entity ID (`Long` or `UUID`) remains identical before and after the update.
   - `repository.count()` does NOT increment (guaranteeing an SQL `UPDATE`, not an accidental `INSERT`).
2. **Persistence Integrity via `EntityManager.clear()`**:
   - The test must call `entityManager.flush()` and `entityManager.clear()` after the `POST` request to evict Hibernate's first-level session cache and re-query the true database state from PostgreSQL.
3. **Relational Linkage Integrity**:
   - Foreign key relationships (e.g. `Book -> Author`, `Book -> Press`, `Game -> Studio`, `Purchase -> Vendor`, `CarTrip -> Car`) are properly re-linked or preserved.
4. **Domain-Specific Invariants**:
   - **Habits**: Invoking `POST /last-done-habit-entries` with a habit ID must insert a new completion log (`entry.setId(null)` fix), whereas `POST /habits` with an ID must update the habit definition in-place.
   - **Contracts**: Updating contract metadata via `POST /contracts` with `id` must preserve cancellation status, distinct from `POST /contracts/{id}/terminate`.
   - **Spatial**: Updating `Point` coordinates must trigger air-distance recalculation on referencing `Route` entities.

---

## 3. Detailed Test Specifications by Domain

### Domain 1: Spend, Commercial Partners & Contracts (`FinanceManagementIT.java`)

Enhance [`FinanceManagementIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/FinanceManagementIT.java) with tests verifying in-place updates:

1. **`testUpdateContractorFlow()`**:
   - Create `Contractor` ("Telekom Deutschland GmbH").
   - Perform `POST /contractors` with `id = contractor.getId()`, `name = "Deutsche Telekom AG"`.
   - Assert `contractorRepository.count() == 1`, `contractor.getName().equals("Deutsche Telekom AG")`.
2. **`testUpdateEmployerFlow()`**:
   - Create `Employer` ("Old Tech Corp").
   - Perform `POST /employers` with `id = employer.getId()`, `name = "New Tech Holding GmbH"`.
   - Assert `employerRepository.count() == 1`, `employer.getName().equals("New Tech Holding GmbH")`.
3. **`testUpdateVendorFlow()`**:
   - Create `Vendor` ("Amazon DE").
   - Perform `POST /vendors` with `id = vendor.getId()`, `name = "Amazon EU S.a.r.l."`.
   - Assert `vendorRepository.count() == 1`, `vendor.getName().equals("Amazon EU S.a.r.l.")`.
4. **`testUpdatePurchaseFlow()`**:
   - Create `Vendor` and `Purchase` ("Logitech MX Master 3S", cost: 99.99).
   - Perform `POST /purchases` with `id = purchase.getId()`, `item = "Logitech MX Master 3S - Graphite"`, `cost = 89.99`, `vendor = vendor.getId()`.
   - Assert `purchaseRepository.count() == 1`, cost and item updated.

---

### Domain 2: Healthcare & Personal Entities (`DoctorControllerIT.java`, `SocialAndPeopleIT.java`)

Enhance [`DoctorControllerIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DoctorControllerIT.java) and [`SocialAndPeopleIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SocialAndPeopleIT.java):

1. **`testUpdateSpecialityFlow()`** (in `DoctorControllerIT`):
   - Create `Speciality` ("General Practice").
   - Perform `POST /specialities` with `id = spec.getId()`, `name = "Internal Medicine & GP"`.
   - Assert `specialityRepository.count() == 1`, updated name persisted.
2. **`testUpdateDoctorFlow()`** (in `DoctorControllerIT`):
   - Create `Doctor` ("Dr. Robert Koch") with speciality.
   - Perform `POST /doctors` with `id = doctor.getId()`, updated office address, phone number, and speciality.
   - Assert `doctorRepository.count() == 1`, updated details in DB.
3. **`testUpdateHumanFlow()`** (in `SocialAndPeopleIT`):
   - Create `Human` ("Marie", "Curie", birthday: 1867-11-07, category: FRIEND).
   - Perform `POST /humans` with `id = human.getId()`, `firstname = "Marie"`, `lastname = "Skłodowska-Curie"`, `category = "COLLEAGUE"`.
   - Assert `humanRepository.count() == 1`, updated name and category persisted.
4. **`testUpdateHumanEventFlow()`** (in `SocialAndPeopleIT`):
   - Create `HumanEvent` ("Nobel Prize Ceremony", date: 1903-12-10).
   - Perform `POST /human-events` with `id = event.getId()`, updated title and description.
   - Assert `humanEventRepository.count() == 1`, updated title persisted.

---

### Domain 3: Vehicles, Stations & Mobility (`TransitAndMobilityIT.java`, `RefuelingIT.java`)

Enhance [`TransitAndMobilityIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java) and [`RefuelingIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/RefuelingIT.java):

1. **`testUpdateCarFlow()`** (in `TransitAndMobilityIT`):
   - Create `Car` ("BMW 320d", plate: "M-SL-101", mileage: 120000).
   - Perform `POST /cars` with `id = car.getId()`, `model = "BMW 320d Touring"`, `mileage = 125000`.
   - Assert `carRepository.count() == 1`, updated mileage and model persisted.
2. **`testUpdateCarTripFlow()`** (in `TransitAndMobilityIT`):
   - Create `CarTrip` between two points.
   - Perform `POST /car-trips` with `id = trip.getId()`, updated comments and distance.
   - Assert `carTripRepository.count() == 1`, updated comments persisted.
3. **`testUpdateTrainLineAndStationFlow()`** (in `TransitAndMobilityIT`):
   - Create `TrainLine` ("RE 1") and `TrainStation2` ("München Hbf").
   - Perform `POST /train-lines` with `id = line.getId()`, `name = "RE 1 (RRX)"`.
   - Perform `POST /train-stations` with `id = station.getId()`, `name = "München Hauptbahnhof"`.
   - Assert counts unchanged, updated names persisted.
4. **`testUpdateStationFlow()`** (in `RefuelingIT`):
   - Create `GasStation` ("Aral Tankstelle", brand: "Aral").
   - Perform `POST /stations/gas` with `id = station.getId()`, `name = "Aral Autohof"`, `street = "Industriestr. 5"`.
   - Assert `stationRepository.count() == 1`, station updated in-place.
5. **`testUpdateRefuelingFlow()`** (in `RefuelingIT`):
   - Create `Refueling` (quantity: 45.0, pricePerUnit: 1.759).
   - Perform `POST /refuelings` with `id = refueling.getId()`, `quantity = 46.50`, `pricePerUnit = 1.749`.
   - Assert `refuelingRepository.count() == 1`, recalculated total cost and consumption correct.

---

### Domain 4: Books, Media & Gaming Ecosystem (`ActivitiesAndMediaIT.java`)

Enhance [`ActivitiesAndMediaIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java):

1. **`testUpdateBookEcosystemFlow()`**:
   - Create `Author` ("George Orwell"), `Press` ("Secker & Warburg"), `Book` ("1984", pages: 328).
   - Update `Author`: `POST /authors` with `id = author.getId()`, `name = "Eric Arthur Blair (George Orwell)"`.
   - Update `Press`: `POST /press` with `id = press.getId()`, `name = "Penguin Books"`.
   - Update `Book`: `POST /books` with `id = book.getId()`, `title = "Nineteen Eighty-Four (Annotated)"`, `pages = 368`.
   - Assert counts of Author, Press, Book unchanged; assert updated fields in DB.
2. **`testUpdateGameEcosystemFlow()`**:
   - Create `Publisher` ("Electronic Arts"), `Studio` ("BioWare"), `Game` ("Mass Effect").
   - Update `Publisher`: `POST /publishers` with `id = publisher.getId()`, `name = "EA Games"`.
   - Update `Studio`: `POST /studios` with `id = studio.getId()`, `name = "BioWare Edmonton"`.
   - Update `Game`: `POST /games` with `id = game.getId()`, `title = "Mass Effect: Legendary Edition"`.
   - Assert single-row semantics and updated titles in DB.
3. **`testUpdateDeviceEcosystemFlow()`**:
   - Create `Manufacturer` ("Apple"), `Device` ("MacBook Pro 14").
   - Update `Manufacturer`: `POST /manufacturers` with `id = mfg.getId()`, `name = "Apple Inc."`.
   - Update `Device`: `POST /devices` with `id = dev.getId()`, `name = "MacBook Pro 14 (M3 Max)"`.
   - Assert counts unchanged, updated names persisted.

---

### Domain 5: Habits, Routines & Well-being (`WellnessAndHabitsIT.java`, `PersonalJournalIT.java`)

Enhance [`WellnessAndHabitsIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java) and [`PersonalJournalIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/PersonalJournalIT.java):

1. **`testUpdateHabitFlowAndCompletionLogSafety()`** (in `WellnessAndHabitsIT`):
   - Create `Habit` ("Morning Meditation", frequency: DAILY).
   - Update Habit definition: `POST /habits` with `id = habit.getId()`, `name = "Mindful Morning Meditation"`.
   - Assert `habitRepository.count() == 1`, habit name updated in DB.
   - Log completion: `POST /last-done-habit-entries` with `id = habit.getId()`.
   - Assert `habitEntryRepository.count() == 1`.
   - Log second completion on next day: `POST /last-done-habit-entries` with `id = habit.getId()`.
   - Assert `habitEntryRepository.count() == 2` (verifying `entry.setId(null)` prevents overwriting previous logs!).
2. **`testUpdateMoodFlow()`** (in `WellnessAndHabitsIT`):
   - Create `Mood` (rating: 3, date: 2026-10-21).
   - Perform `POST /moods` with `id = mood.getId()`, `rating = 5`, `comment = "Significant breakthrough on architectural plan!"`.
   - Assert `moodRepository.count() == 1`, rating 5 and comment persisted.
3. **`testUpdateDiaryAndGratitudeFlow()`** (in `PersonalJournalIT`):
   - Create `DiaryEntry` and `Gratitude`.
   - Perform `POST /diary` with `id = diary.getId()`, updated text description.
   - Perform `POST /gratitudes` with `id = gratitude.getId()`, updated gratitude statement.
   - Assert counts unchanged, updated texts persisted.

---

## 4. Execution Sequence & Validation

Each test file will be augmented and validated sequentially:

1. **Phase 1**: Spend & Commercial Partners (`FinanceManagementIT.java`)
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.FinanceManagementIT -x jacocoTestReport
   ```
2. **Phase 2**: Healthcare & Personal Records (`DoctorControllerIT.java`, `SocialAndPeopleIT.java`)
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.DoctorControllerIT --tests de.oberamsystems.slm.SocialAndPeopleIT -x jacocoTestReport
   ```
3. **Phase 3**: Vehicles & Mobility (`TransitAndMobilityIT.java`, `RefuelingIT.java`)
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.TransitAndMobilityIT --tests de.oberamsystems.slm.RefuelingIT -x jacocoTestReport
   ```
4. **Phase 4**: Books, Media & Gaming Ecosystem (`ActivitiesAndMediaIT.java`)
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.ActivitiesAndMediaIT -x jacocoTestReport
   ```
5. **Phase 5**: Habits, Routines & Well-being (`WellnessAndHabitsIT.java`, `PersonalJournalIT.java`)
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.WellnessAndHabitsIT --tests de.oberamsystems.slm.PersonalJournalIT -x jacocoTestReport
   ```
6. **Phase 6**: Full Regression Suite
   ```bash
   JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test -x jacocoTestReport
   ```
