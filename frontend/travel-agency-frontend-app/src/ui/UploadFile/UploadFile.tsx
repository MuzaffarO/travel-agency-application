import { Upload } from "lucide-react";
import toast from "react-hot-toast";

type UploadFileProps = {
  className?: string;
  onValidFile?: (file: File) => void;
  id?: string;
};

const UploadFile = ({
  className,
  onValidFile,
  id = "file-upload",
}: UploadFileProps) => {
  const handleFile = (file: File | undefined) => {
    if (!file) return;

    const allowedTypes = ["application/pdf", "image/jpeg", "image/png"];
    if (!allowedTypes.includes(file.type)) {
      toast.error("Only PDF, JPG, and PNG files are allowed.");
      return;
    }

    const maxSizeMB = 10;
    if (file.size > maxSizeMB * 1024 * 1024) {
      toast.error(`File size must be less than ${maxSizeMB} MB.`);
      return;
    }
    console.log("File is valid and will be processed:", file);
    onValidFile?.(file);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    handleFile(file);
    e.target.value = "";
  };

  const handleDrop = (e: React.DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    e.stopPropagation();

    const file = e.dataTransfer.files[0];
    handleFile(file);
  };

  const handleDragOver = (e: React.DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    e.stopPropagation();
  };

  return (
    <div className={`w-full ${className}`}>
      <label
        htmlFor={id}
        className="flex flex-col items-center justify-center w-full h-24 border border-grey-05 rounded-[8px] cursor-pointer bg-white hover:bg-gray-50 transition"
        onDrop={handleDrop}
        onDragOver={handleDragOver}
      >
        <Upload size={24} className="text-blue-09 mb-2" />
        <p className="text-sm font-bold text-blue-09 ">
          Click to upload or drag and drop
        </p>
        <input
          id={id}
          name={id}
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          className="hidden"
          onChange={handleFileChange}
        />
      </label>
    </div>
  );
};

export default UploadFile;
