import { BACK_URL } from "../constants.ts";
import type { Tour } from "../pages/MainPage.tsx";

export type ApiResponse = {
  tours: Tour[];
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
};

export const fetchTours = async (
  queryParams: Record<string, any> = {}
): Promise<ApiResponse> => {
  const cleanedParams: Record<string, string> = {};


  Object.entries(queryParams).forEach(([key, value]) => {
    if (value === null || value === undefined) return; // skip null/undefined
    if (Array.isArray(value) && value.length === 0) return; // skip empty arrays
    cleanedParams[key] = Array.isArray(value) ? value.join(",") : String(value);
  });

  cleanedParams["pageSize"] = "10";

  const queryString = new URLSearchParams(cleanedParams).toString();

  const response = await fetch(`${BACK_URL}/tours/available?${queryString}`);
  console.log(response);
  if (!response.ok) {
    throw new Error(`HTTP error. Status: ${response.status}`);
  }

  return await response.json();
};
