import { ref, computed } from "vue";
import { endpointByType } from "../constants/dataConstants";

export function useDashboard() {
  const statistics = ref(null);
  const records = ref([]);
  const isLoadingRecords = ref(false);

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

  async function fetchRecords(selectedDataTypeName) {
    isLoadingRecords.value = true;

    try {
      const endpoint = endpointByType[selectedDataTypeName.value];
      const response = await fetch(
        `${endpoint}?page=0&size=5&sortBy=id&sortDir=desc`,
      );
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

  async function refreshDashboard(selectedDataTypeName) {
    await Promise.all([fetchStatistics(), fetchRecords(selectedDataTypeName)]);
  }

  return {
    statistics,
    records,
    isLoadingRecords,
    importedTotal,
    fetchStatistics,
    fetchRecords,
    refreshDashboard,
  };
}
