<script setup>
import { computed, ref } from "vue";
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardList,
  Database,
  FileSpreadsheet,
  Gauge,
  Loader2,
  Server,
  Upload,
  XCircle,
} from "lucide-vue-next";

const selectedFile = ref(null);
const result = ref(null);
const errorMessage = ref("");
const successMessage = ref("");
const isUploading = ref(false);
const healthStatus = ref("Not checked");

// Mirrors the backend's current CSV contract while the domain schema is still evolving.
const sampleCsv = `name,email,age
Dealer Contact,dealer.ops@example.com,42
Warranty Analyst,warranty@example.com,36
Fleet Coordinator,fleet@example.com,29`;

const hasResult = computed(() => Boolean(result.value));
const hasValidationErrors = computed(() => result.value?.errors?.length > 0);
const uploadDisabled = computed(() => !selectedFile.value || isUploading.value);

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

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] ?? null;
  result.value = null;
  errorMessage.value = "";
  successMessage.value = "";
}

// Quick connectivity check for operators before attempting an upload.
async function checkHealth() {
  healthStatus.value = "Checking";

  // Add a small delay to make the checking animation more visible
  await new Promise((resolve) => setTimeout(resolve, 1500));

  try {
    const response = await fetch("/health");
    healthStatus.value = response.ok ? "Online" : "Unavailable";
  } catch {
    healthStatus.value = "Unavailable";
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
    // Browser can reach backend at localhost:8081 when frontend is at localhost:5173
    const response = await fetch("http://localhost:8081/api/upload", {
      method: "POST",
      body: formData,
    });

    const payload = await response.json();
    result.value = payload;

    if (!response.ok && !payload.errors?.length) {
      errorMessage.value = "The import could not be completed.";
    } else if (response.ok || payload.errors?.length > 0) {
      // Show success message and clear file selection for new upload
      successMessage.value = `Successfully uploaded "${fileName}". ${payload.rowsImported} rows imported, ${payload.rowsRejected} rows rejected.`;
      selectedFile.value = null;
    }
  } catch {
    errorMessage.value =
      "The API is not reachable. Check that the backend is running.";
  } finally {
    isUploading.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen bg-[#eef2f5] text-[#17202a]">
    <!-- Header and API health panel -->
    <section class="border-b border-[#cfd8df] bg-white">
      <div
        class="mx-auto flex max-w-7xl flex-col gap-6 px-5 py-6 md:px-8 lg:flex-row lg:items-center lg:justify-between"
      >
        <div>
          <p
            class="text-sm font-semibold uppercase tracking-[0.12em] text-[#1d6f78]"
          >
            Data Platform
          </p>
          <h1 class="mt-2 text-3xl font-semibold text-[#17202a] md:text-4xl">
            Automotive data ingestion console
          </h1>
          <p class="mt-3 max-w-3xl text-base leading-7 text-[#536270]">
            Upload dealership, fleet, warranty, or customer contact data and
            inspect the import result before it moves further through the
            platform.
          </p>
        </div>

        <div class="grid min-w-72 grid-cols-2 gap-3">
          <div class="rounded-md border border-[#d8e0e6] bg-[#f8fafb] p-4">
            <div
              class="flex items-center gap-2 text-sm font-medium text-[#536270]"
            >
              <Server class="h-4 w-4 text-[#1d6f78]" />
              API
            </div>
            <div class="mt-2 flex items-center gap-3">
              <div
                class="h-4 w-4 rounded-full transition-all duration-300"
                :class="[
                  healthStatusColor,
                  healthStatus === 'Checking' ? 'animate-pulse' : '',
                ]"
              ></div>
              <p class="text-xl font-semibold text-[#17202a]">
                {{ healthStatus }}
              </p>
            </div>
          </div>
          <button
            class="rounded-md bg-[#17202a] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#263442] hover:cursor-pointer disabled:bg-[#7b8791]"
            type="button"
            @click="checkHealth"
          >
            Check API
          </button>
        </div>
      </div>
    </section>

    <!-- Upload workspace and operational status panels -->
    <section
      class="mx-auto grid max-w-7xl gap-5 px-5 py-6 md:px-8 lg:grid-cols-[1.2fr_0.8fr]"
    >
      <div class="rounded-md border border-[#cfd8df] bg-white p-5 shadow-sm">
        <div
          class="flex flex-col gap-4 border-b border-[#e2e8ed] pb-5 md:flex-row md:items-center md:justify-between"
        >
          <div>
            <div
              class="flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.12em] text-[#1d6f78]"
            >
              <FileSpreadsheet class="h-4 w-4" />
              CSV import
            </div>
            <h2 class="mt-2 text-2xl font-semibold text-[#17202a]">
              Upload source data
            </h2>
          </div>
          <div
            class="rounded-md bg-[#f3f6f8] px-3 py-2 text-sm font-medium text-[#536270]"
          >
            Accepted: name, email, age
          </div>
        </div>

        <div class="mt-5 grid gap-5 lg:grid-cols-[1fr_0.9fr]">
          <div>
            <label
              class="flex min-h-56 cursor-pointer flex-col items-center justify-center rounded-md border-2 border-dashed border-[#9eb3c1] bg-[#f8fafb] px-5 py-8 text-center transition hover:border-[#1d6f78] hover:bg-[#f1f7f8]"
            >
              <Upload class="h-9 w-9 text-[#1d6f78]" />
              <span class="mt-4 text-lg font-semibold text-[#17202a]">
                {{ selectedFile ? selectedFile.name : "Choose a CSV file" }}
              </span>
              <span class="mt-2 max-w-md text-sm leading-6 text-[#536270]">
                Current backend schema accepts contact-style records.
                Vehicle-specific fields will be added in the next backend
                iteration.
              </span>
              <input
                class="sr-only"
                type="file"
                accept=".csv,text/csv"
                @change="handleFileChange"
              />
            </label>

            <button
              class="mt-4 inline-flex h-11 w-full items-center justify-center gap-2 rounded-md bg-[#1d6f78] px-4 text-sm font-semibold text-white transition hover:bg-[#185d65] disabled:cursor-not-allowed disabled:bg-[#9eb3c1]"
              type="button"
              :disabled="uploadDisabled"
              @click="uploadFile"
            >
              <Loader2 v-if="isUploading" class="h-4 w-4 animate-spin" />
              <Upload v-else class="h-4 w-4" />
              {{ isUploading ? "Importing" : "Upload CSV" }}
            </button>
          </div>

          <div
            class="rounded-md border border-[#d8e0e6] bg-[#101820] p-4 text-sm text-[#e7eef3]"
          >
            <div class="mb-3 flex items-center gap-2 text-[#8bd3dd]">
              <ClipboardList class="h-4 w-4" />
              <span class="font-semibold">Sample feed</span>
            </div>
            <pre class="overflow-x-auto whitespace-pre-wrap leading-6">{{
              sampleCsv
            }}</pre>
          </div>
        </div>
      </div>

      <aside class="grid gap-5">
        <div class="rounded-md border border-[#cfd8df] bg-white p-5 shadow-sm">
          <div
            class="flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.12em] text-[#1d6f78]"
          >
            <Gauge class="h-4 w-4" />
            Import summary
          </div>

          <div v-if="hasResult" class="mt-5 grid grid-cols-3 gap-3">
            <div class="rounded-md bg-[#f3f6f8] p-3">
              <p class="text-sm text-[#536270]">Read</p>
              <p class="mt-1 text-2xl font-semibold">{{ result.rowsRead }}</p>
            </div>
            <div class="rounded-md bg-[#eff8f2] p-3">
              <p class="text-sm text-[#416b4b]">Imported</p>
              <p class="mt-1 text-2xl font-semibold text-[#276738]">
                {{ result.rowsImported }}
              </p>
            </div>
            <div class="rounded-md bg-[#fff5e5] p-3">
              <p class="text-sm text-[#815d18]">Rejected</p>
              <p class="mt-1 text-2xl font-semibold text-[#9b650c]">
                {{ result.rowsRejected }}
              </p>
            </div>
          </div>

          <div
            v-else
            class="mt-5 rounded-md bg-[#f8fafb] p-4 text-sm leading-6 text-[#536270]"
          >
            No import has been run in this session.
          </div>

          <div
            v-if="errorMessage"
            class="mt-4 flex gap-2 rounded-md bg-[#fff1f0] p-3 text-sm text-[#9f2d22]"
          >
            <XCircle class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ errorMessage }}</span>
          </div>

          <div
            v-if="successMessage"
            class="mt-4 flex gap-2 rounded-md bg-[#eff8f2] p-3 text-sm text-[#276738]"
          >
            <CheckCircle2 class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ successMessage }}</span>
          </div>
        </div>

        <div class="rounded-md border border-[#cfd8df] bg-white p-5 shadow-sm">
          <div
            class="flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.12em] text-[#1d6f78]"
          >
            <Database class="h-4 w-4" />
            Pipeline status
          </div>

          <div class="mt-5 space-y-3">
            <div
              class="flex items-center justify-between rounded-md bg-[#f3f6f8] px-3 py-3"
            >
              <span class="text-sm font-medium text-[#536270]"
                >PostgreSQL persistence</span
              >
              <CheckCircle2 class="h-5 w-5 text-[#276738]" />
            </div>
            <div
              class="flex items-center justify-between rounded-md bg-[#f3f6f8] px-3 py-3"
            >
              <span class="text-sm font-medium text-[#536270]"
                >Row validation</span
              >
              <CheckCircle2 class="h-5 w-5 text-[#276738]" />
            </div>
            <div
              class="flex items-center justify-between rounded-md bg-[#f3f6f8] px-3 py-3"
            >
              <span class="text-sm font-medium text-[#536270]"
                >Redis summary cache</span
              >
              <AlertTriangle class="h-5 w-5 text-[#9b650c]" />
            </div>
          </div>
        </div>
      </aside>
    </section>

    <!-- Validation report is shown only when the API rejects one or more rows -->
    <section
      v-if="hasValidationErrors"
      class="mx-auto max-w-7xl px-5 pb-8 md:px-8"
    >
      <div class="rounded-md border border-[#e5b8a8] bg-white p-5 shadow-sm">
        <div
          class="flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.12em] text-[#9f2d22]"
        >
          <AlertTriangle class="h-4 w-4" />
          Validation errors
        </div>

        <div class="mt-4 overflow-hidden rounded-md border border-[#ead4cc]">
          <table class="w-full table-fixed border-collapse text-left text-sm">
            <thead class="bg-[#fff1f0] text-[#7d281f]">
              <tr>
                <th class="w-24 px-4 py-3 font-semibold">Row</th>
                <th class="w-36 px-4 py-3 font-semibold">Field</th>
                <th class="px-4 py-3 font-semibold">Issue</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="error in result.errors"
                :key="`${error.rowNumber}-${error.field}-${error.message}`"
                class="border-t border-[#ead4cc]"
              >
                <td class="px-4 py-3 font-medium">{{ error.rowNumber }}</td>
                <td class="px-4 py-3">{{ error.field }}</td>
                <td class="px-4 py-3">{{ error.message }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </main>
</template>
