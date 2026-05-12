<script setup>
import { ref, computed, watch, nextTick } from "vue";
import { ChevronDown, Check } from "lucide-vue-next";

const props = defineProps({
  modelValue: {
    type: [String, Number],
    required: true,
  },
  options: {
    type: Array,
    required: true,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
    default: "Select an option",
  },
});

const emit = defineEmits(["update:modelValue"]);

const isOpen = ref(false);
const selectRef = ref(null);

const selectedOption = computed(() => {
  return props.options.find((opt) => opt.value === props.modelValue) || null;
});

const toggleDropdown = () => {
  if (!props.disabled) {
    isOpen.value = !isOpen.value;
  }
};

const selectOption = (option) => {
  emit("update:modelValue", option.value);
  isOpen.value = false;
};

const handleClickOutside = (event) => {
  if (selectRef.value && !selectRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

watch(isOpen, (newVal) => {
  if (newVal) {
    nextTick(() => {
      document.addEventListener("click", handleClickOutside);
    });
  } else {
    document.removeEventListener("click", handleClickOutside);
  }
});
</script>

<template>
  <div ref="selectRef" class="animated-select">
    <button
      type="button"
      class="select-trigger"
      :disabled="disabled"
      @click="toggleDropdown"
    >
      <span class="select-value">
        {{ selectedOption ? selectedOption.label : placeholder }}
      </span>
      <ChevronDown
        class="select-arrow"
        :class="{ 'rotate-180': isOpen }"
      />
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="dropdown-menu">
        <div
          v-for="(option, index) in options"
          :key="option.value"
          class="dropdown-item"
          :class="{ 'is-selected': option.value === modelValue }"
          v-motion
          :initial="{ opacity: 0, x: -10 }"
          :enter="{
            opacity: 1,
            x: 0,
            transition: { duration: 200, delay: index * 30, easing: 'ease-out' },
          }"
          @click="selectOption(option)"
        >
          <span class="dropdown-item-label">{{ option.label }}</span>
          <Check v-if="option.value === modelValue" class="dropdown-item-check" />
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.animated-select {
  position: relative;
  width: 100%;
}

.select-trigger {
  width: 100%;
  height: 2.85rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0.85rem;
  border: 1px solid var(--border-strong);
  border-radius: 8px;
  background: var(--surface-solid);
  color: var(--text);
  font-size: 1rem;
  transition:
    border-color 200ms ease,
    box-shadow 200ms ease,
    transform 200ms ease;
  cursor: pointer;
}

.select-trigger:hover:not(:disabled) {
  border-color: var(--brand);
  transform: translateY(-1px);
}

.select-trigger:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--brand) 26%, transparent);
  outline-offset: 2px;
  border-color: var(--brand);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--brand) 18%, transparent);
}

.select-trigger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.select-value {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-arrow {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
  transition: transform 200ms ease;
  color: var(--text-muted);
}

.select-arrow.rotate-180 {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  left: 0;
  right: 0;
  z-index: 50;
  background: var(--surface-solid);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  max-height: 240px;
  overflow-y: auto;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 0.85rem;
  cursor: pointer;
  transition:
    background 150ms ease,
    color 150ms ease;
  gap: 0.5rem;
}

.dropdown-item:hover {
  background: var(--surface-muted);
  color: var(--brand);
}

.dropdown-item.is-selected {
  background: color-mix(in srgb, var(--brand-soft) 50%, transparent);
  color: var(--brand);
}

.dropdown-item-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.95rem;
}

.dropdown-item-check {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition:
    opacity 200ms ease,
    transform 200ms ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
