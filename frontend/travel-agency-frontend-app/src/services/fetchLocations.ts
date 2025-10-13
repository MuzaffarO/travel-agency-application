import axios from "axios";
import {BACK_URL} from "../constants.ts";

export const fetchLocations = async (destination: string): Promise<string[]> => {
  if (!destination || destination.length < 3) {
    return [];
  }

  const response = await axios.get(`${BACK_URL}/tours/destinations`, {
    params: { destination },
  });

  return response.data.destinations || [];
};