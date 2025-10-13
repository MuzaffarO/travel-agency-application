import axios from "axios";
import {BACK_URL} from "../constants.ts";
import globalRouter from "../globalRouter.ts";

const api = axios.create({
  baseURL: BACK_URL,
  headers: {"Content-Type": "application/json"},
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.code === "ERR_NETWORK" && globalRouter.navigate) {
      localStorage.clear();
      globalRouter.navigate("/login");
    }
    return Promise.reject(error);
  },
);

export default api;
