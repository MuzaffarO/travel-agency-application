import { BACK_URL } from "../constants";
import type { Booking } from "./getBookings";

type UploadPayload = {
  payments: {
    fileName: string;
    type: string;
    base64encodedDocument: string;
  }[];
  guestDocuments: {
    userName: string;
    documents: {
      fileName: string;
      type: string;
      base64encodedDocument: string;
    }[];
  }[];
};

const fileToBase64 = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      const base64String = (reader.result as string).split(",")[1];
      resolve(base64String);
    };
    reader.onerror = (error) => reject(error);
  });

export const uploadDocuments = async (
  bookingId: Booking["id"],
  passportFiles: File[][],
  paymentFiles: File[],
  token: string,
  guestTabs: string[]
): Promise<void> => {
  const payments = await Promise.all(
    paymentFiles.map(async (file) => ({
      fileName: file.name,
      type: file.type.split("/")[1],
      base64encodedDocument: await fileToBase64(file),
    }))
  );

  const guestDocuments = await Promise.all(
    passportFiles.map(async (guestFiles, index) => {
      const documents = await Promise.all(
        guestFiles.map(async (file) => ({
          fileName: file.name,
          type: file.type.split("/")[1],
          base64encodedDocument: await fileToBase64(file),
        }))
      );

      return {
        userName: guestTabs[index],
        documents,
      };
    })
  );

  const payload: UploadPayload = {
    payments,
    guestDocuments,
  };

  const response = await fetch(`${BACK_URL}/bookings/${bookingId}/documents`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error("Failed to upload documents");
  }
};
