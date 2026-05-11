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
import { useTheme } from "./composables/useTheme";
import { useApi } from "./composables/useApi";
import { useDataTypes } from "./composables/useDataTypes";
import { useFileUpload } from "./composables/useFileUpload";
import { useDashboard } from "./composables/useDashboard";

const validationErrorsSection = ref(null);

// Theme composable
const { isDarkMode, themeLabel, toggleTheme } = useTheme();

// API composable
const { healthStatus, checkHealth } = useApi();

// Data types composable
const {
  dataTypes,
  selectedDataTypeName,
  selectedDataType,
  selectedSchemaFields,
  sampleCsv,
  recordColumns,
  isLoadingDataTypes,
  fetchDataTypes,
} = useDataTypes();

// File upload composable
const {
  selectedFile,
  result,
  errorMessage,
  successMessage,
  isUploading,
  hasResult,
  hasValidationErrors,
  uploadDisabled,
  handleFileChange,
  uploadFile,
} = useFileUpload(selectedDataTypeName, selectedDataType);

// Dashboard composable
const {
  statistics,
  records,
  isLoadingRecords,
  importedTotal,
  fetchStatistics,
  fetchRecords,
  refreshDashboard,
} = useDashboard();

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
  fetchRecords(selectedDataTypeName);
});

const setErrorMessage = (msg) => {
  errorMessage.value = msg;
};

onMounted(async () => {
  await Promise.all([
    checkHealth(),
    fetchDataTypes(setErrorMessage),
    refreshDashboard(selectedDataTypeName),
  ]);
});
</script>

<template>
  <main class="app-shell" :class="{ 'theme-dark': isDarkMode }">
    <section class="hero-panel">
      <div class="page-frame hero-layout">
        <div class="hero-copy">
          <h1>Data Platform</h1>
          <p class="hero-subtitle">Automotive data ingestion console</p>
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
                :class="[
                  healthStatusColor,
                  healthStatus === 'Checking' ? 'animate-pulse' : '',
                ]"
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
            <h2 class="inline-eyebrow">
              <FileSpreadsheet class="icon-small" />
              CSV import
            </h2>
            <p class="panel-subtitle">Upload automotive source data</p>
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
          <div class="upload-section">
            <label class="drop-zone compact">
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
              @click="
                () => uploadFile(() => refreshDashboard(selectedDataTypeName))
              "
            >
              <Loader2 v-if="isUploading" class="icon-small animate-spin" />
              <Upload v-else class="icon-small" />
              {{ isUploading ? "Importing" : "Upload CSV" }}
            </button>
          </div>

          <div class="code-panel expanded">
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
            <pre class="sample-feed">{{ sampleCsv }}</pre>
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
              @click="() => refreshDashboard(selectedDataTypeName)"
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
                  <th v-for="column in recordColumns" :key="column">
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
                <tr v-for="record in records" v-else :key="record.id">
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
