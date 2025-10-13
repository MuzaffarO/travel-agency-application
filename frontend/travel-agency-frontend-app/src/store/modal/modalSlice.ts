import { createSlice, type Draft, type PayloadAction } from "@reduxjs/toolkit";

export type ModalName =
  | "bookingForm"
  | "IsNotLogged"
  | "cancelTour"
  | "confirmReserve"
  | "feedback"
  | "uploadDocs"
  | null;

export interface UploadedFile {
  name: string;
  size: number;
  type: string;
  url: string;
}

export interface UploadDocsProps {
  passportUploaded: boolean;
  paymentUploaded: boolean;
  passportFile?: UploadedFile | null;
  paymentFile?: UploadedFile | null;
}

export interface CancelTourProps {
  bookingId: string;
  tourName: string;
  startDate: string;
  duration: string;
  mealPlan: string;
  adults: number;
  children?: number;
}

export interface ConfirmReserveProps {
  tourName: string;
  startDate: string;
  duration: string;
  mealPlan: string;
  adults: number;
  children: number;
}

export interface BookingModalProps {
  tourId: string;
  tourName: string;
  rating: number;
  priceFrom: number;
  destination: string;
  durations?: string[];
  startDate?: string;
  mealPlans?: string[];
}

export type EmptyModalProps = Record<string, never>;

export type ModalPropsMap = {
  bookingForm: BookingModalProps;
  IsNotLogged: EmptyModalProps;
  cancelTour: CancelTourProps;
  confirmReserve: ConfirmReserveProps;
  feedback: EmptyModalProps;
  uploadDocs: UploadDocsProps;
};

interface ModalState {
  activeModal: ModalName;
  modalProps:
    | BookingModalProps
    | ConfirmReserveProps
    | CancelTourProps
    | EmptyModalProps
    | UploadDocsProps
    | null;
}

const initialState: ModalState = {
  activeModal: null,
  modalProps: null,
};
const modalSlice = createSlice({
  name: "modal",
  initialState,
  reducers: {
    openModal: <T extends keyof ModalPropsMap>(
      state: Draft<ModalState>,
      action: PayloadAction<{ name: T; props: ModalPropsMap[T] }>
    ) => {
      state.activeModal = action.payload.name;
      state.modalProps = action.payload.props;
    },
    closeModal: (state: Draft<ModalState>) => {
      state.activeModal = null;
      state.modalProps = null;
    },
  },
});

export const { openModal, closeModal } = modalSlice.actions;
export default modalSlice.reducer;
