import { ref } from "vue";

export function useApi() {
  const healthStatus = ref("Not checked");

  async function checkHealth() {
    healthStatus.value = "Checking";

    try {
      const response = await fetch("/health");
      healthStatus.value = response.ok ? "Online" : "Unavailable";
    } catch {
      healthStatus.value = "Unavailable";
    }
  }

  return {
    healthStatus,
    checkHealth,
  };
}
