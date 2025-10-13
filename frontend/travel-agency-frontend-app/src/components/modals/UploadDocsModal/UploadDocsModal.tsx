import { useState, useEffect } from "react";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import UploadFile from "../../../ui/UploadFile";
import UploadedDocumentItem from "../../UploadedDocumentItem";
import { CircleCheck } from "lucide-react";
import type { Booking } from "../../../services/getBookings";
import { Loader2 } from "lucide-react";
import SuccessToast from "../../../ui/SuccessToast";
import { toast } from "react-hot-toast";
import { uploadDocuments } from "../../../services/uploadDocuments";

type UploadDocsModalProps = {
  booking: Booking;
  onClose: () => void;
};

const UploadDocsModal = ({ booking, onClose }: UploadDocsModalProps) => {
  const [activeTab, setActiveTab] = useState<"passport" | "payment">(
    "passport"
  );
  const [activeGuestIndex, setActiveGuestIndex] = useState(0);

  const [passportFiles, setPassportFiles] = useState<File[][]>([]);
  const [paymentFiles, setPaymentFiles] = useState<File[]>([]);
  const [isUploading, setIsUploading] = useState(false);

  const guestTabs =
    booking.customerDetails?.documents.guestDocuments.map((g) => g.userName) ||
    [];

  useEffect(() => {
    setPassportFiles(Array.from({ length: guestTabs.length }, () => []));
  }, [booking, guestTabs.length]);

  useEffect(() => {
    if (
      activeTab === "payment" &&
      passportFiles.every((arr) => arr.length === 0)
    ) {
      setActiveTab("passport");
    }
  }, [activeTab, passportFiles]);

  const handleTabClick = (tab: "passport" | "payment") => {
    if (tab === "payment" && passportFiles.every((arr) => arr.length === 0))
      return;
    setActiveTab(tab);
  };

  const handleAddFile = async (file: File, type: "passport" | "payment") => {
    setIsUploading(true);

    await new Promise((resolve) => setTimeout(resolve, 1500));

    if (type === "passport") {
      setPassportFiles((prev) => {
        const copy = [...prev];
        copy[activeGuestIndex] = [...copy[activeGuestIndex], file];
        return copy;
      });
    } else {
      setPaymentFiles((prev) => [...prev, file]);
    }

    setIsUploading(false);
  };

  const handleRemoveFile = (name: string, type: "passport" | "payment") => {
    if (type === "passport") {
      setPassportFiles((prev) => {
        const copy = [...prev];
        copy[activeGuestIndex] = copy[activeGuestIndex].filter(
          (f) => f.name !== name
        );
        return copy;
      });
    } else {
      setPaymentFiles((prev) => prev.filter((f) => f.name !== name));
    }
  };

  const handleSave = async () => {
    const allPassportsUploaded = passportFiles.every((arr) => arr.length > 0);
    const paymentUploaded = paymentFiles.length > 0;

    if (!allPassportsUploaded || !paymentUploaded) {
      toast.error("Please upload all required documents");
      return;
    }

    try {
      setIsUploading(true);
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const token = user?.token;

      await uploadDocuments(
        booking.id,
        passportFiles,
        paymentFiles,
        token,
        guestTabs
      );

      toast.custom(
        (t) => (
          <SuccessToast
            t={t}
            title="Success"
            message="Your documents have been uploaded successfully"
          />
        ),
        { duration: 5000 }
      );

      onClose();
    } catch (error) {
      console.error(error);
      toast.error("Something went wrong while uploading documents");
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      className="p-6 w-full h-full md:w-3xl md:h-auto"
    >
      <h2 className="text-2xl font-extrabold">Upload Documents</h2>

      <section className="grid grid-cols-[1fr_2fr] gap-6 mt-8 h-[30vh]">
        <div className="flex flex-col gap-3 text-sm border-r border-grey-05 pr-6">
          {guestTabs.map((guest, index) => (
            <p
              key={index}
              className={`relative flex justify-between pb-3 font-extrabold cursor-pointer ${
                activeTab === "passport" && activeGuestIndex === index
                  ? "after:content-[''] after:absolute after:left-0 after:bottom-0 after:w-full after:h-[5px] after:rounded-[6px] after:bg-blue-05"
                  : ""
              } ${passportFiles[index]?.length > 0 ? "text-grey-07" : ""}`}
              onClick={() => {
                setActiveTab("passport");
                setActiveGuestIndex(index);
              }}
            >
              Passport {guest}
              {passportFiles[index]?.length > 0 && (
                <CircleCheck color="#118819" />
              )}
            </p>
          ))}

          <p
            className={`relative flex justify-between pb-3 font-extrabold cursor-pointer ${
              activeTab === "payment"
                ? "after:content-[''] after:absolute after:left-0 after:bottom-0 after:w-full after:h-[5px] after:rounded-[6px] after:bg-blue-05"
                : ""
            } ${passportFiles.every((arr) => arr.length === 0) ? "text-grey-07 cursor-not-allowed" : ""}`}
            onClick={() => handleTabClick("payment")}
          >
            Payment confirmation{" "}
            {paymentFiles.length > 0 && <CircleCheck color="#118819" />}
          </p>
        </div>

        <div className="flex-1 gap-2 flex flex-col">
          <h3 className="text-sm font-extrabold">Add attachments</h3>

          <UploadFile
            id={`file-upload-${activeTab}`}
            className="mb-8"
            onValidFile={(file) => handleAddFile(file, activeTab)}
          />

          {isUploading ? (
            <div className="flex items-center justify-center p-4">
              <Loader2 className="animate-spin h-6 w-6 text-blue-05" />
            </div>
          ) : (
            (activeTab === "passport"
              ? passportFiles[activeGuestIndex]
              : paymentFiles
            )?.map((file) => (
              <UploadedDocumentItem
                key={`${file.name}-${file.lastModified}`}
                file={file}
                onRemove={() => handleRemoveFile(file.name, activeTab)}
              />
            ))
          )}
        </div>
      </section>

      <div className="flex justify-end gap-2 mt-8">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button
          onClick={handleSave}
          disabled={passportFiles.every((arr) => arr.length === 0)}
        >
          Save
        </Button>
      </div>
    </Modal>
  );
};

export default UploadDocsModal;
