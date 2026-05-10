import { ref, computed } from "vue";

export function useFileUpload(selectedDataTypeName, selectedDataType) {
  const selectedFile = ref(null);
  const result = ref(null);
  const errorMessage = ref("");
  const successMessage = ref("");
  const isUploading = ref(false);

  const hasResult = computed(() => Boolean(result.value));
  const hasValidationErrors = computed(() => result.value?.errors?.length > 0);
  const uploadDisabled = computed(() => !selectedFile.value || isUploading.value);

  function handleFileChange(event) {
    selectedFile.value = event.target.files?.[0] ?? null;
    result.value = null;
    errorMessage.value = "";
    successMessage.value = "";
  }

  async function uploadFile(selectedDataTypeName, selectedDataType, refreshDashboard) {
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
      const response = await fetch(
        `/api/automotive/upload/${selectedDataTypeName.value}`,
        {
          method: "POST",
          body: formData,
        },
      );

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
      errorMessage.value =
        "The API is not reachable. Check that the backend is running.";
    } finally {
      isUploading.value = false;
    }
  }

  return {
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
  };
}
