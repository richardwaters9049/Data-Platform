import { ref, computed } from "vue";
import { sampleCsvByType, recordColumnsByType } from "../constants/dataConstants";

export function useDataTypes() {
  const dataTypes = ref([]);
  const selectedDataTypeName = ref("VEHICLE");
  const isLoadingDataTypes = ref(false);

  const selectedDataType = computed(() =>
    dataTypes.value.find((type) => type.name === selectedDataTypeName.value),
  );

  const selectedSchemaFields = computed(
    () => selectedDataType.value?.fields ?? [],
  );

  const sampleCsv = computed(
    () => sampleCsvByType[selectedDataTypeName.value] ?? "",
  );

  const recordColumns = computed(() => {
    return (
      recordColumnsByType[selectedDataTypeName.value] ??
      recordColumnsByType.VEHICLE
    );
  });

  async function fetchDataTypes(setErrorMessage) {
    isLoadingDataTypes.value = true;

    try {
      const response = await fetch("/api/automotive/data-types");
      if (!response.ok) {
        throw new Error("Could not load data types");
      }
      dataTypes.value = await response.json();
    } catch (error) {
      if (setErrorMessage) {
        setErrorMessage("Automotive data types could not be loaded.");
      }
      throw error;
    } finally {
      isLoadingDataTypes.value = false;
    }
  }

  return {
    dataTypes,
    selectedDataTypeName,
    selectedDataType,
    selectedSchemaFields,
    sampleCsv,
    recordColumns,
    isLoadingDataTypes,
    fetchDataTypes,
  };
}
