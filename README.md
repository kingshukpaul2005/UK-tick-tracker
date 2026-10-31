# UK Tick Sighting Tracker

**Author:** Kingshuk Paul Peash
**Submission:** Elanco ITSP Technical Task 2026 (Full Stack option)

An MVP web app that shows tick sightings across the UK on an interactive map, lets people learn how to recognise and avoid ticks, and lets them report new sightings.

## What it does

| Area | Features |
| --- | --- |
| Interactive map | UK map (Leaflet / OpenStreetMap). One bubble per city, sized by number of sightings and coloured by the most common species. Filters for date range and species. |
| Sighting information | Click a city to open a side panel with a species breakdown, recent activity, and **Report**, **Get directions** and **Share** buttons. |
| Educational content | Species guide (5 species), bite-prevention and tick-removal advice, and a seasonal activity chart with location and species filters. |
| Report a sighting | Form with date, time, city, species and optional photo. Client-side and server-side validation with error, warning and success messages. |

## Architecture

```
Browser (HTML + CSS + JS, Leaflet, Chart.js)
        |  JSON over HTTP / multipart form for reports
        v
Spring Boot REST API (Java 17)
  controllers -> services -> Spring Data JPA repositories -> H2 database (file)
        ^
        |  ingestion service (retry, fallback)
Elanco API  (https://dev-task.elancoapps.com/data/tick-sightings)
```

- **Back end** (`back-end/`): Spring Boot 3, Maven. Packages: controllers, services, repositories, entities, `stats`, exception handling.
- **Front end** (`front-end/`): plain HTML, CSS and JavaScript with no build step.
- **Design** (`design/`): wireframes. [CHECK: add once drawn]

### Database
- `sighting`: id (primary key), date, location, species, latin_name, region, latitude, longitude, source (`API` or `USER`), image_path, created_at. Indexed on date and location.
- `city_lookup`: city, region, latitude, longitude.

### API endpoints
| Method and path | Purpose |
| --- | --- |
| `GET /api/sightings` | Filter by `from`, `to`, `city`, `region`, `species`; paginated with `page` and `size` |
| `POST /api/sightings` | Submit a new sighting (multipart form: date, time, city, species, optional image) |
| `GET /api/cities`, `GET /api/species` | Values for dropdowns and the map |
| `GET /api/stats/by-region` | Sighting count per region |
| `GET /api/stats/trends?granularity=week\|month` | Sightings over time, optionally filtered by city, region, species, date range |
| `GET /api/stats/seasonal` | Sightings per calendar month (feeds the seasonal chart) |
| `POST /api/ingest/refresh` | Re-fetch data from the Elanco API |

## Technical choices and why

- **Java 21 and Spring Boot:** the language I am strongest in. Spring gives REST, validation, CORS and error handling with little code.
- **H2 file database:** needs no install, so the project runs from a clean clone. The database file is git-ignored. For production I would move to PostgreSQL; the JPA code would barely change.
- **Spring Data JPA:** filtering, pagination and indexes with annotations rather than hand-written SQL.
- **Plain JavaScript:** no build tooling to break, and the whole front end can be read in a few files.
- **Leaflet and Chart.js:** small, well-documented libraries loaded from a CDN.
- **City lookup table:** the Elanco data has only a city name, with no coordinates or region. I added a lookup table for the 14 cities in the data, with city-centre coordinates and region.

## Data handling

**Fetching.** The Elanco API returns every record in one response and is rate limited, so the app fetches once, stores the records in its own database, and serves users from there. It never calls Elanco per user request. Ingestion runs at start-up when the database is empty, and any time via `POST /api/ingest/refresh`.

**API failure.** Each call has a connect and read timeout and is retried a configurable number of times, waiting longer after each failure. If every attempt fails, the app loads the bundled copy of the data (`back-end/samples/tick-sightings.json`) instead, logs why, and says so in the response (`dataSource` is `SEED_FILE` and `message` explains the failure). Data already stored is never removed.

**Duplicates.**
- A record with an id already stored, or repeated in the same load, is skipped and counted.
- A record with the same day, city and species as another in the same load is kept but logged as a suspected duplicate, because two real sightings can share those values.

**Incomplete or invalid records.** Each record is checked before it is stored. It is rejected, logged with the reason, and counted if it is null, has a missing or over-long id, has a missing date, location or species, has a date that cannot be parsed or is in the future, or names a city that is not in the lookup table. The response of every ingestion reports how many records were received, saved, skipped as duplicates and rejected.

**An honest note on the data.** I analysed the full sample of 1,000 records. It was clean: unique ids, no missing values, valid dates (2012 to 2024), 14 cities and 5 species with consistent Latin names. The ingestion is still defensive, and unit tests use deliberately bad records to prove the checks work. One real inconsistency: Elanco's data pairs "Passerine tick" with *Dermacentor frontalis*, while GOV.UK lists the passerine tick as *Ixodes frontalis*. I kept the Elanco value as supplied and noted the difference in the species guide.

**User submissions.** The server checks every field, rejects future dates, and rejects unknown cities or species. Images are limited to 5 MB and must be genuinely JPEG, PNG or WebP: the server checks the file's first bytes, not just its name or content type. Files are stored under random names. Errors use one consistent JSON format, with a message per field.

## Accessibility
- Labelled form fields, with errors linked to their fields and announced to screen readers.
- Skip-to-content link and visible keyboard focus.
- Fully keyboard-operable tabs, buttons and filters.
- Status messages use ARIA live regions.
- The seasonal chart has a text description and a data table alternative.
- Species are shown by name as well as colour, so colour is never the only signal.

## Running the project

### Requirements
- Java 17 or later (the Maven wrapper is included; no separate Maven install)
- Node.js, only if you use the `npx serve` option for the front end

### 1. Start the back end
```
cd back-end
mvnw.cmd spring-boot:run        (Windows)
./mvnw spring-boot:run          (macOS / Linux)
```
The API runs at http://localhost:8080. On first start it loads the sighting data. Wait for the "Started ... Application" log line.

### 2. Start the front end
Serve the `front-end` folder over HTTP. **Do not open `index.html` by double-clicking it:** OpenStreetMap blocks map tiles for pages opened from `file://`.

Either:
```
npx serve front-end -l 5500
```
or open the folder in VS Code and use the Live Server extension. Then go to http://localhost:5500.

### 3. Run the tests
```
cd back-end
mvnw.cmd test
```
The tests cover record validation (missing fields, bad date, future date, unknown city) and ingestion (duplicate ids, rejected records, fallback to the seed file when the Elanco API fails).

## What is done and what is not

**Done:** every back-end requirement (ingestion, duplicates and incomplete data, report endpoint, filtering, region and trend aggregates, error handling) and at least two requirements in each of the four front-end areas.

**Scheduled background refresh (ingestion runs at start-up when the database is empty, or on demand)**
- The Elanco data has no severity, so the map shows species and sighting count, not severity.
- Authentication, moderation of user reports, and rate limiting on the report endpoint.
- Automated front-end tests.
- Scheduled background refresh of the Elanco data (refresh is on start-up or via the refresh endpoint).

## What I would improve with more time
- Restrict CORS to the real front-end origin (it is open for local development).
- Move to PostgreSQL and store images in object storage.
- Moderation queue for user reports, and a warning when a new report closely matches an existing one.
- Geocode user locations instead of a fixed city list, and cluster markers at high zoom.
- Front-end tests and a CI pipeline.

## Sources for educational content
- [GOV.UK: Tick species profiles](https://gov.uk/guidance/tick-species-profiles)
- [nidirect: Protect yourself against tick bites](https://nidirect.gov.uk/news/protect-yourself-against-tick-bites)

The Learn tab is general information, not medical advice.
