import { createApp } from "vue";
import { MotionPlugin } from "@vueuse/motion";
import App from "./App.vue";
import "./style.css";

// Single-page Vue entry point.
const app = createApp(App);
app.use(MotionPlugin);
app.mount("#app");
