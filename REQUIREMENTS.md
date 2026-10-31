# Requirements: UK Tick Sighting Tracker

**Author:** Kingshuk Paul Peash
**Project type:** Full-stack web application (MVP)
**Data source:** https://dev-task.elancoapps.com/

## 1. Purpose

Ticks are becoming more common across the UK and can carry diseases such as Lyme disease. This project is a Minimum Viable Product (MVP) of a web application that:

- collects and cleans tick sighting data from an external API,
- shows sightings on an interactive map of the UK,
- lets people report new sightings, and
- gives pet owners and vets insight into where and when ticks are active.

## 2. Users

| User | Needs |
| --- | --- |
| Pet owner | See nearby tick activity, learn how to identify ticks and prevent bites, report a sighting |
| Veterinarian | Explore sightings by area and time, see trends |
| Contributor | Submit a new sighting quickly, with a photo |

## 3. Repository structure

```
/
├── README.md          Project overview, technical choices, run instructions
├── REQUIREMENTS.md    This document
├── back-end/          API, data processing and database
├── front-end/         Web interface
└── design/            Wireframes and UX notes
```

## 4. Scope

**In scope:** data ingestion and cleaning, a REST API, an interactive map with filters, sighting details, educational content, and a sighting report form with validation and image upload.

**Out of scope for the MVP:** user accounts, moderation of submitted reports, notifications, and production deployment.

## 5. Back-end requirements

Each requirement is ticked when it is implemented.

### 5.1 Data handling and processing

- [ ] **BE-1** Fetch sighting data from the external API and store a local copy in the project's own database.
- [ ] **BE-2** Handle large datasets with paginated requests and responses, and with database indexes on date and location.
- [ ] **BE-3** Detect and remove duplicate sightings, using a documented definition of a duplicate.
- [ ] **BE-4** Validate every record. Records with missing or invalid fields are rejected, flagged or given safe defaults, and the decision is logged.
- [ ] **BE-5** Assign each sighting to a UK region so counts per region are possible.

### 5.2 API endpoints

- [ ] **BE-6** Accept a new sighting report from the front-end form (date, time, location, species and optional image).
- [ ] **BE-7** Return sightings filtered by time range.
- [ ] **BE-8** Return sightings filtered by location (region, city or map area).
- [ ] **BE-9** Return the number of sightings per region.
- [ ] **BE-10** Return sighting trends over time, grouped by week or month.
- [ ] **BE-11** Store images uploaded with a report and make them available to the front end.
- [ ] **BE-12** Allow the front end to call the API from a different origin (CORS).

### 5.3 Error handling

- [ ] **BE-13** If the external API fails or times out, retry and then fall back to the stored data, with a clear message.
- [ ] **BE-14** Data inconsistencies do not stop the system. Bad records are skipped and logged.
- [ ] **BE-15** Invalid submissions get a clear error message and an appropriate HTTP status code (for example 400 or 422).
- [ ] **BE-16** All endpoints return errors in the same format.

### 5.4 Optional back-end extensions

- [ ] **BE-17** An AI or machine-learning insight endpoint, for example identifying areas with unusual increases.

## 6. Front-end requirements

The MVP must include at least two requirements from each of the four areas below.

### 6.1 Interactive map

- [ ] **FE-1** A dynamic map showing tick sightings across the UK.
- [ ] **FE-2** Markers with visual indicators such as colour or icon by species, severity or recent activity.
- [ ] **FE-3** Filter controls for date range and species.

### 6.2 Sighting information

- [ ] **FE-4** A side panel or modal showing details of a sighting when its marker is clicked.
- [ ] **FE-5** Quick action buttons: report a sighting, get directions, and share.
- [ ] **FE-6** A timeline of activity for a selected area.

### 6.3 Educational content

- [ ] **FE-7** A guide to identifying common UK tick species.
- [ ] **FE-8** Tips for preventing tick bites.
- [ ] **FE-9** A chart of seasonal tick activity for a chosen location and year.

### 6.4 Report a sighting

- [ ] **FE-10** A form to report a sighting with date, time, location and species.
- [ ] **FE-11** Form validation with clear error, warning and success messages.
- [ ] **FE-12** Image upload with the report.

### 6.5 Optional front-end extensions

- [ ] **FE-13** Accessibility features, such as keyboard navigation, text alternatives for images, form labels, sufficient colour contrast and ARIA attributes.
- [ ] **FE-14** Wireframes in the `design/` folder.

## 7. Non-functional requirements

- [ ] **NF-1** The application runs locally by following the README, starting from a clean clone.
- [ ] **NF-2** The interface is usable on desktop and mobile screens.
- [ ] **NF-3** The front end shows loading and error states when the API is slow or unavailable.
- [ ] **NF-4** No secrets, keys or personal data are committed to the repository.
- [ ] **NF-5** Code is organised, readable and has meaningful commit history.
- [ ] **NF-6** The core back-end behaviour (validation, filtering and aggregation) has automated tests.

## 8. Technical stack

Technology choices and the reasons for them are in the [README](README.md).

## 9. Deliverables

- [ ] Public GitHub repository with all code.
- [ ] `README.md` with the project overview, technical choices and clear instructions for running it.
- [ ] A walkthrough video of no more than 10 minutes.
- [ ] Optional documentation of architecture decisions, how the system consumes and presents data, and what would be improved with more time.

## 10. Status

This table is updated as work is completed.

| Area | Done | Total |
| --- | --- | --- |
| Back end (BE-1 to BE-16) | 0 | 16 |
| Front end (FE-1 to FE-12) | 0 | 12 |
| Non-functional (NF-1 to NF-6) | 0 | 6 |
| Optional extras (BE-17, FE-13, FE-14) | 0 | 3 |
