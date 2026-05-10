import { ref, computed } from "vue";

export function useTheme() {
  const isDarkMode = ref(false);

  const themeLabel = computed(() =>
    isDarkMode.value ? "Light mode" : "Dark mode",
  );

  function toggleTheme() {
    isDarkMode.value = !isDarkMode.value;
  }

  return {
    isDarkMode,
    themeLabel,
    toggleTheme,
  };
}
