<script setup>
import { computed, onMounted, ref, watch, nextTick } from "vue";
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardList,
  Database,
  FileSpreadsheet,
  Gauge,
  Loader2,
  Moon,
  RefreshCw,
  Server,
  Sun,
  Upload,
  XCircle,
} from "lucide-vue-next";

const isDarkMode = ref(false);
const dataTypes = ref([]);
const selectedDataTypeName = ref("VEHICLE");
const selectedFile = ref(null);
const result = ref(null);
const errorMessage = ref("");
const successMessage = ref("");
const isUploading = ref(false);
const isLoadingDataTypes = ref(false);
const isLoadingRecords = ref(false);
const healthStatus = ref("Not checked");
const statistics = ref(null);
const records = ref([]);
const validationErrorsSection = ref(null);

const sampleCsvByType = {
  VEHICLE: `vin,make,model,year,trim,color,fuelType,transmission,engineSize,bodyStyle,dealerCode,status
SALAB2BN1HH123456,Jaguar,F-Pace,2024,R-Dynamic,Blue,Petrol,Automatic,2.0,SUV,DLR001,Active`,
  DEALER: `code,name,address,city,state,zipCode,phone,email,website,status
DLR001,North Wales Jaguar,Parc Menai,Bangor,Gwynedd,LL57 4BN,01248 000000,ops@nwjaguar.example,nwjaguar.example,Active`,
  WARRANTY: `warrantyNumber,warrantyType,startDate,endDate,mileageLimit,coverage,deductible,vin,provider,status
WRN-1001,Extended,2026-01-01,2029-01-01,60000,Powertrain,250,SALAB2BN1HH123456,Manufacturer,Active`,
  FLEET: `fleetCode,fleetName,company,address,city,state,zipCode,contactPerson,contactPhone,contactEmail,vehicleCount,status
FLT001,Executive Vehicles,Example Logistics,Parc Menai,Bangor,Gwynedd,LL57 4BN,A Morgan,01248 111111,fleet@example.com,24,Active`,
  SERVICE_RECORD: `serviceNumber,serviceType,serviceDate,mileage,description,cost,vin,dealerCode,technician,status
SRV-1001,Oil Change,2026-02-14,12000,Scheduled service,189.99,SALAB2BN1HH123456,DLR001,A Morgan,Completed`,
};

const endpointByType = {
  VEHICLE: "/api/records/vehicles",
  DEALER: "/api/records/dealers",
  WARRANTY: "/api/records/warranties",
  FLEET: "/api/records/fleets",
  SERVICE_RECORD: "/api/records/services",
};

const selectedDataType = computed(() =>
  dataTypes.value.find((type) => type.name === selectedDataTypeName.value)
);
const selectedSchemaFields = computed(() => selectedDataType.value?.fields ?? []);
const sampleCsv = computed(() => sampleCsvByType[selectedDataTypeName.value] ?? "");
const hasResult = computed(() => Boolean(result.value));
const hasValidationErrors = computed(() => result.value?.errors?.length > 0);
const uploadDisabled = computed(() => !selectedFile.value || isUploading.value);
const themeLabel = computed(() => isDarkMode.value ? "Light mode" : "Dark mode");
const importedTotal = computed(() => {
  if (!statistics.value) {
    return 0;
  }

  return [
    statistics.value.totalVehicles,
    statistics.value.totalDealers,
    statistics.value.totalWarranties,
    statistics.value.totalFleets,
    statistics.value.totalServices,
  ].reduce((total, value) => total + Number(value ?? 0), 0);
});

const recordColumns = computed(() => {
  switch (selectedDataTypeName.value) {
    case "DEALER":
      return ["code", "name", "city", "status"];
    case "WARRANTY":
      return ["warrantyNumber", "warrantyType", "provider", "status"];
    case "FLEET":
      return ["fleetCode", "fleetName", "company", "status"];
    case "SERVICE_RECORD":
      return ["serviceNumber", "serviceType", "technician", "status"];
    case "VEHICLE":
    default:
      return ["vin", "make", "model", "status"];
  }
});

const healthStatusColor = computed(() => {
  switch (healthStatus.value) {
    case "Online":
      return "bg-green-500";
    case "Checking":
      return "bg-yellow-500";
    case "Unavailable":
    case "Not checked":
      return "bg-red-500";
    default:
      return "bg-gray-500";
  }
});

watch(hasValidationErrors, (hasErrors) => {
  if (hasErrors) {
    nextTick(() => {
      validationErrorsSection.value?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    });
  }
});

watch(selectedDataTypeName, () => {
  result.value = null;
  errorMessage.value = "";
  successMessage.value = "";
  selectedFile.value = null;
  fetchRecords();
});

onMounted(async () => {
  await Promise.all([checkHealth(), fetchDataTypes(), refreshDashboard()]);
});

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] ?? null;
  result.value = null;
  errorMessage.value = "";
  successMessage.value = "";
}

async function checkHealth() {
  healthStatus.value = "Checking";

  try {
    const response = await fetch("/health");
    healthStatus.value = response.ok ? "Online" : "Unavailable";
  } catch {
    healthStatus.value = "Unavailable";
  }
}

async function fetchDataTypes() {
  isLoadingDataTypes.value = true;

  try {
    const response = await fetch("/api/automotive/data-types");
    if (!response.ok) {
      throw new Error("Could not load data types");
    }
    dataTypes.value = await response.json();
  } catch {
    errorMessage.value = "Automotive data types could not be loaded.";
  } finally {
    isLoadingDataTypes.value = false;
  }
}

async function refreshDashboard() {
  await Promise.all([fetchStatistics(), fetchRecords()]);
}

async function fetchStatistics() {
  try {
    const response = await fetch("/api/records/statistics/overall");
    if (response.ok) {
      statistics.value = await response.json();
    }
  } catch {
    statistics.value = null;
  }
}

async function fetchRecords() {
  isLoadingRecords.value = true;

  try {
    const endpoint = endpointByType[selectedDataTypeName.value];
    const response = await fetch(`${endpoint}?page=0&size=5&sortBy=id&sortDir=desc`);
    if (!response.ok) {
      throw new Error("Could not load records");
    }
    const payload = await response.json();
    records.value = payload.content ?? [];
  } catch {
    records.value = [];
  } finally {
    isLoadingRecords.value = false;
  }
}

async function uploadFile() {
  if (!selectedFile.value) {
    return;
  }

  isUploading.value = true;
  result.value = null;
  errorMessage.value = "";
  successMessage.value = "";

  const formData = new FormData();
  formData.append("file", selectedFile.value);
  const fileName = selectedFile.value.name;

  try {
    const response = await fetch(`/api/automotive/upload/${selectedDataTypeName.value}`, {
      method: "POST",
      body: formData,
    });

    const payload = await response.json();
    result.value = payload;

    if (!response.ok && !payload.errors?.length) {
      errorMessage.value = "The import could not be completed.";
    } else {
      successMessage.value = `${fileName} processed as ${selectedDataType.value?.displayName ?? selectedDataTypeName.value}. ${payload.rowsImported} rows imported, ${payload.rowsRejected} rejected.`;
      selectedFile.value = null;
      await refreshDashboard();
    }
  } catch {
    errorMessage.value = "The API is not reachable. Check that the backend is running.";
  } finally {
    isUploading.value = false;
  }
}

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value;
}
</script>

<template>
  <main class="app-shell" :class="{ 'theme-dark': isDarkMode }">
    <section class="hero-panel">
      <div class="page-frame hero-layout">
        <div class="hero-copy">
          <p class="eyebrow">
            Data Platform
          </p>
          <h1>
            Automotive data ingestion console
          </h1>
          <p class="hero-text">
            Load dealer, vehicle, warranty, fleet, and service feeds into the
            ETL pipeline, review validation output, and inspect the latest
            persisted records.
          </p>
        </div>

        <div class="hero-actions">
          <div class="status-card">
            <div class="label-row">
              <Server class="icon-small" />
              API
            </div>
            <div class="status-value">
              <div
                class="status-dot"
                :class="[healthStatusColor, healthStatus === 'Checking' ? 'animate-pulse' : '']"
              ></div>
              <p>
                {{ healthStatus }}
              </p>
            </div>
          </div>
          <button
            class="button button-secondary"
            type="button"
            @click="checkHealth"
          >
            <RefreshCw class="icon-small" />
            Check API
          </button>
          <button
            class="button button-ghost"
            type="button"
            :aria-label="themeLabel"
            @click="toggleTheme"
          >
            <Sun v-if="isDarkMode" class="icon-small" />
            <Moon v-else class="icon-small" />
            {{ themeLabel }}
          </button>
        </div>
      </div>
    </section>

    <section class="page-frame dashboard-grid">
      <div class="panel import-panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow inline-eyebrow">
              <FileSpreadsheet class="icon-small" />
              CSV import
            </div>
            <h2>
              Upload automotive source data
            </h2>
          </div>
          <label class="field-label">
            Data type
            <select
              v-model="selectedDataTypeName"
              class="select-control"
              :disabled="isLoadingDataTypes"
            >
              <option
                v-for="dataType in dataTypes"
                :key="dataType.name"
                :value="dataType.name"
              >
                {{ dataType.displayName }}
              </option>
            </select>
          </label>
        </div>

        <div class="import-grid">
          <div>
            <label class="drop-zone">
              <Upload class="drop-icon" />
              <span class="drop-title">
                {{ selectedFile ? selectedFile.name : "Choose a CSV file" }}
              </span>
              <span class="drop-copy">
                The selected file will run through ingestion, validation,
                transformation, persistence, and statistics cache refresh.
              </span>
              <input
                class="sr-only"
                type="file"
                accept=".csv,text/csv"
                @change="handleFileChange"
              />
            </label>

            <button
              class="button button-primary upload-button"
              type="button"
              :disabled="uploadDisabled"
              @click="uploadFile"
            >
              <Loader2 v-if="isUploading" class="icon-small animate-spin" />
              <Upload v-else class="icon-small" />
              {{ isUploading ? "Importing" : "Upload CSV" }}
            </button>
          </div>

          <div class="code-panel">
            <div class="code-heading">
              <ClipboardList class="icon-small" />
              <span>Expected schema</span>
            </div>
            <div class="schema-list">
              <span
                v-for="field in selectedSchemaFields"
                :key="field"
                class="schema-chip"
              >
                {{ field }}
              </span>
            </div>
            <div class="code-heading">
              <ClipboardList class="icon-small" />
              <span>Sample feed</span>
            </div>
            <pre>{{ sampleCsv }}</pre>
          </div>
        </div>
      </div>

      <aside class="side-stack">
        <div class="panel summary-panel">
          <div class="eyebrow inline-eyebrow">
            <Gauge class="icon-small" />
            Import summary
          </div>

          <div v-if="hasResult" class="metric-grid">
            <div class="metric-tile neutral">
              <p class="metric-label">Read</p>
              <p class="metric-value">{{ result.rowsRead }}</p>
            </div>
            <div class="metric-tile success">
              <p class="metric-label">Imported</p>
              <p class="metric-value">
                {{ result.rowsImported }}
              </p>
            </div>
            <div class="metric-tile warning">
              <p class="metric-label">Rejected</p>
              <p class="metric-value">
                {{ result.rowsRejected }}
              </p>
            </div>
          </div>

          <div v-else class="empty-state">
            No import has been run in this session.
          </div>

          <div v-if="errorMessage" class="message message-error">
            <XCircle class="message-icon" />
            <span>{{ errorMessage }}</span>
          </div>

          <div v-if="successMessage" class="message message-success">
            <CheckCircle2 class="message-icon" />
            <span>{{ successMessage }}</span>
          </div>
        </div>

        <div class="panel snapshot-panel">
          <div class="section-toolbar">
            <div class="eyebrow inline-eyebrow">
              <Database class="icon-small" />
              Platform snapshot
            </div>
            <button
              class="button button-outline"
              type="button"
              @click="refreshDashboard"
            >
              <RefreshCw class="icon-tiny" />
              Refresh
            </button>
          </div>

          <div class="snapshot-stats">
            <div class="snapshot-stat">
              <p>Stored rows</p>
              <strong>{{ importedTotal }}</strong>
            </div>
            <div class="snapshot-stat">
              <p>Current view</p>
              <strong>{{ records.length }}</strong>
            </div>
          </div>

          <div class="table-shell">
            <table>
              <thead>
                <tr>
                  <th
                    v-for="column in recordColumns"
                    :key="column"
                  >
                    {{ column }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isLoadingRecords">
                  <td :colspan="recordColumns.length">
                    Loading latest records...
                  </td>
                </tr>
                <tr v-else-if="records.length === 0">
                  <td :colspan="recordColumns.length">
                    No records found for this data type.
                  </td>
                </tr>
                <tr
                  v-for="record in records"
                  v-else
                  :key="record.id"
                >
                  <td
                    v-for="column in recordColumns"
                    :key="column"
                    :title="String(record[column] ?? '')"
                  >
                    {{ record[column] ?? "-" }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </aside>
    </section>

    <section
      v-if="hasValidationErrors"
      ref="validationErrorsSection"
      class="page-frame validation-section"
    >
      <div class="panel validation-panel">
        <div class="eyebrow danger inline-eyebrow">
          <AlertTriangle class="icon-small" />
          Validation errors
        </div>

        <div class="table-shell validation-table">
          <table>
            <thead>
              <tr>
                <th>Row</th>
                <th>Field</th>
                <th>Issue</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="error in result.errors"
                :key="`${error.rowNumber}-${error.field}-${error.message}`"
              >
                <td>{{ error.rowNumber }}</td>
                <td>{{ error.field }}</td>
                <td>{{ error.message }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </main>
</template>
