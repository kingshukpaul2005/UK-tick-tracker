"use strict";

// ---------- Settings ----------
const API_BASE = "http://localhost:8080";
const PAGE_SIZE = 500;
const FALLBACK_COLOUR = "#666666";
const SPECIES_COLOURS = {
    "Marsh tick": "#1b9e77",
    "Southern rodent tick": "#d95f02",
    "Passerine tick": "#7570b3",
    "Tree-hole tick": "#e7298a",
    "Fox/badger tick": "#66a61e"
};

// ---------- Page elements ----------
const statusEl = document.getElementById("status");
const panelEl = document.getElementById("panel");
const cityListEl = document.getElementById("city-list");
const legendEl = document.getElementById("legend");
const fromEl = document.getElementById("from");
const toEl = document.getElementById("to");
const speciesEl = document.getElementById("species");

// ---------- Map ----------
const map = L.map("map").setView([54.3, -2.5], 6);
L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 18,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);
const markerLayer = L.layerGroup().addTo(map);

// ---------- Helpers ----------
function colourFor(species) {
    return SPECIES_COLOURS[species] || FALLBACK_COLOUR;
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text == null ? "" : String(text);
    return div.innerHTML;
}

function formatDate(isoString) {
    return new Date(isoString).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
}

function setStatus(message, options = {}) {
    statusEl.textContent = message;
    statusEl.classList.toggle("error", Boolean(options.error));
    if (options.retry) {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = "Try again";
        button.addEventListener("click", options.retry);
        statusEl.append(" ", button);
    }
}

// ---------- Talking to the back end ----------
async function api(path) {
    let response;
    try {
        response = await fetch(API_BASE + path);
    } catch (networkError) {
        throw new Error("Cannot reach the server. Is the back end running on " + API_BASE + "?");
    }
    if (!response.ok) {
        let message = "Server error (" + response.status + ")";
        try {
            const body = await response.json();
            if (body.message) message = body.message;
        } catch (parseError) {
            // keep the default message
        }
        throw new Error(message);
    }
    return response.json();
}

// The API returns pages, so keep asking until we have every page.
async function fetchAllSightings(filters) {
    const all = [];
    let page = 0;
    let totalPages = 1;
    while (page < totalPages) {
        const query = new URLSearchParams({ ...filters, page: page, size: PAGE_SIZE });
        const data = await api("/api/sightings?" + query);
        all.push(...data.content);
        totalPages = data.totalPages;
        page++;
    }
    return all;
}

function currentFilters() {
    const filters = {};
    if (fromEl.value) filters.from = fromEl.value;
    if (toEl.value) filters.to = toEl.value;
    if (speciesEl.value) filters.species = speciesEl.value;
    return filters;
}

// ---------- Turning sightings into city bubbles ----------
function groupByCity(sightings) {
    const groups = new Map();
    for (const s of sightings) {
        if (!groups.has(s.location)) {
            groups.set(s.location, { city: s.location, region: s.region, lat: s.latitude, lng: s.longitude, sightings: [] });
        }
        groups.get(s.location).sightings.push(s);
    }
    return [...groups.values()].sort((a, b) => b.sightings.length - a.sightings.length);
}

function countBySpecies(sightings) {
    const counts = {};
    for (const s of sightings) {
        counts[s.species] = (counts[s.species] || 0) + 1;
    }
    return Object.entries(counts).sort((a, b) => b[1] - a[1]);   // biggest first
}

function render(sightings) {
    markerLayer.clearLayers();
    cityListEl.replaceChildren();

    for (const group of groupByCity(sightings)) {
        const total = group.sightings.length;
        const topSpecies = countBySpecies(group.sightings)[0][0];

        const marker = L.circleMarker([group.lat, group.lng], {
            radius: 6 + Math.sqrt(total) * 2.2,
            color: "#222222",
            weight: 1,
            fillColor: colourFor(topSpecies),
            fillOpacity: 0.75
        });
        marker.bindTooltip(group.city + ": " + total + " sightings");
        marker.on("click", () => showPanel(group));
        marker.addTo(markerLayer);

        const item = document.createElement("li");
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = group.city + " (" + total + ")";
        button.addEventListener("click", () => showPanel(group));
        item.append(button);
        cityListEl.append(item);
    }
}

// ---------- Side panel ----------
function showPanel(group) {
    const total = group.sightings.length;
    const breakdown = countBySpecies(group.sightings)
        .map(([name, count]) =>
            '<li><span class="swatch" style="background:' + colourFor(name) + '"></span>' +
            escapeHtml(name) + ": " + count + "</li>")
        .join("");
    const recent = group.sightings.slice(0, 10)          // the API returns newest first
        .map(s => "<li>" + formatDate(s.date) + " &ndash; " + escapeHtml(s.species) +
            (s.source === "USER" ? " (user report)" : "") + "</li>")
        .join("");
    const directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + group.lat + "," + group.lng;

    panelEl.innerHTML =
        "<h3>" + escapeHtml(group.city) + "</h3>" +
        '<p class="muted">' + escapeHtml(group.region) + " &middot; " + total + " sightings match your filters</p>" +
        "<h4>Species seen</h4><ul class=\"breakdown\">" + breakdown + "</ul>" +
        "<h4>Recent activity</h4><ul class=\"timeline\">" + recent + "</ul>" +
        '<div class="panel-actions">' +
        '<button type="button" id="report-btn">Report a sighting here</button>' +
        '<a href="' + directionsUrl + '" target="_blank" rel="noopener">Get directions</a>' +
        '<button type="button" id="share-btn">Share</button>' +
        "</div>";

    document.getElementById("report-btn").addEventListener("click", () => {
        switchTab("report-section");
        document.dispatchEvent(new CustomEvent("report-city", { detail: group.city }));
    });
    document.getElementById("share-btn").addEventListener("click", () => shareArea(group));
    panelEl.focus();
}

async function shareArea(group) {
    const text = group.sightings.length + " tick sightings recorded near " + group.city + " (UK Tick Tracker)";
    try {
        if (navigator.share) {
            await navigator.share({ title: "UK Tick Tracker", text: text, url: location.href });
        } else {
            await navigator.clipboard.writeText(text + " " + location.href);
            setStatus("Link copied to your clipboard.");
        }
    } catch (cancelledOrBlocked) {
        setStatus("Sharing was cancelled or is not available in this browser.");
    }
}

// ---------- Loading data ----------
async function refresh() {
    const filters = currentFilters();
    if (filters.from && filters.to && filters.from > filters.to) {
        setStatus("The start date must not be after the end date.", { error: true });
        return;
    }
    setStatus("Loading sightings...");
    try {
        const sightings = await fetchAllSightings(filters);
        render(sightings);
        panelEl.innerHTML = "<p>Select a bubble on the map to see details for that area.</p>";
        setStatus(sightings.length === 0
            ? "No sightings match these filters."
            : sightings.length + " sightings shown.");
    } catch (error) {
        setStatus(error.message, { error: true, retry: refresh });
    }
}

async function loadSpecies() {
    const names = await api("/api/species");
    for (const name of names) {
        const option = document.createElement("option");
        option.value = name;
        option.textContent = name;
        speciesEl.append(option);
        fieldEl("species").append(option.cloneNode(true));   // report form dropdown

        const key = document.createElement("span");
        key.innerHTML = '<span class="swatch" style="background:' + colourFor(name) + '"></span>' + escapeHtml(name);
        legendEl.append(key);
    }
}

// ---------- Tabs ----------
function switchTab(id) {
    document.querySelectorAll(".tab-panel").forEach(p => { p.hidden = p.id !== id; });
    document.querySelectorAll(".tab").forEach(t => {
        const active = t.dataset.tab === id;
        t.classList.toggle("active", active);
        t.setAttribute("aria-current", active ? "page" : "false");
    });
    if (id === "map-section") map.invalidateSize();   // Leaflet needs this after being hidden
}

document.querySelectorAll(".tab").forEach(t => {
    t.addEventListener("click", () => switchTab(t.dataset.tab));
});

// ---------- Report form ----------
const formEl = document.getElementById("report-form");
const formSummaryEl = document.getElementById("form-summary");
const submitBtn = document.getElementById("r-submit");
const FIELDS = ["city", "date", "time", "species", "image"];
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

function fieldEl(name) { return document.getElementById("r-" + name); }
function errorEl(name) { return document.getElementById("r-" + name + "-error"); }

function todayString() {                       // local date, not UTC
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return now.getFullYear() + "-" + month + "-" + day;
}

function showSummary(message, kind) {          // kind is "error" or "success"
    formSummaryEl.className = kind;
    formSummaryEl.textContent = message;
}

function clearErrors() {
    FIELDS.forEach(name => {
        errorEl(name).textContent = "";
        fieldEl(name).removeAttribute("aria-invalid");
    });
    formSummaryEl.className = "";
    formSummaryEl.textContent = "";
}

function showErrors(errors) {
    let first = null;
    for (const [name, message] of Object.entries(errors)) {
        if (FIELDS.includes(name)) {
            errorEl(name).textContent = message;
            fieldEl(name).setAttribute("aria-invalid", "true");
            first = first || name;
        }
    }
    showSummary("Your report was not sent. Please fix the highlighted fields.", "error");
    if (first) fieldEl(first).focus();
}

// Quick checks in the browser for a friendly experience; the server checks everything again.
function validateReport() {
    const errors = {};
    if (!fieldEl("city").value) errors.city = "Please choose a city.";
    if (!fieldEl("species").value) errors.species = "Please choose a species.";
    const date = fieldEl("date").value;
    if (!date) errors.date = "Please enter the date you saw the tick.";
    else if (date > todayString()) errors.date = "The date cannot be in the future.";
    const file = fieldEl("image").files[0];
    if (file) {
        if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
            errors.image = "Image must be a JPEG, PNG or WebP file.";
        } else if (file.size > MAX_IMAGE_BYTES) {
            errors.image = "Image must be 5 MB or smaller.";
        }
    }
    return errors;
}

// Warning (does not block sending): date more than a year ago
fieldEl("date").addEventListener("change", () => {
    const value = fieldEl("date").value;
    const limit = new Date();
    limit.setFullYear(limit.getFullYear() - 1);
    document.getElementById("r-date-warning").textContent =
        value && new Date(value) < limit
            ? "Warning: this date is more than a year ago. Please check it is correct."
            : "";
});

async function loadCities() {
    const cities = await api("/api/cities");
    const names = cities.map(c => (typeof c === "string" ? c : c.city)).sort();
    for (const name of names) {
        const option = document.createElement("option");
        option.value = name;
        option.textContent = name;
        fieldEl("city").append(option);
    }
}

formEl.addEventListener("submit", async event => {
    event.preventDefault();
    clearErrors();

    const problems = validateReport();
    if (Object.keys(problems).length > 0) {
        showErrors(problems);
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Sending...";
    try {
        const response = await fetch(API_BASE + "/api/sightings", {
            method: "POST",
            body: new FormData(formEl)         // do not set Content-Type yourself
        });
        const body = await response.json().catch(() => ({}));

        if (response.status === 201) {
            formEl.reset();
            document.getElementById("r-date-warning").textContent = "";
            showSummary("Thank you! Your sighting in " + body.location + " has been recorded.", "success");
            formSummaryEl.focus();
            await refresh();                   // so the new report appears on the map
        } else if (body.fieldErrors) {
            showErrors(body.fieldErrors);
        } else {
            showSummary(body.message || "Server error (" + response.status + ")", "error");
        }
    } catch (networkError) {
        showSummary("Cannot reach the server. Is the back end running on " + API_BASE + "?", "error");
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = "Submit report";
    }
});

// "Report a sighting here" button in the map panel pre-fills the city
document.addEventListener("report-city", event => { fieldEl("city").value = event.detail; });

// ---------- Wiring and start ----------
[fromEl, toEl, speciesEl].forEach(el => el.addEventListener("change", refresh));
document.getElementById("reset-filters").addEventListener("click", () => {
    fromEl.value = "";
    toEl.value = "";
    speciesEl.value = "";
    refresh();
});

async function init() {
    fieldEl("date").max = todayString();
    try {
        if (speciesEl.options.length <= 1) await loadSpecies();
    } catch (error) {
        setStatus(error.message, { error: true, retry: init });
        return;
    }
    try {
        if (fieldEl("city").options.length <= 1) await loadCities();
    } catch (error) {
        showSummary("Could not load the city list: " + error.message, "error");
    }
    await refresh();
}

init();