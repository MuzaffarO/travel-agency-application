import { File, Trash } from "lucide-react";

export type UploadedDocumentItemProps = {
  file: File | { name: string; size: number; type?: string; url?: string };
  onRemove?: () => void;
};

const UploadedDocumentItem = ({
  file,
  onRemove,
}: UploadedDocumentItemProps) => {
  const sizeKB = file.size ? (file.size / 1024).toFixed(0) : "0";
  return (
    <section className="flex items-center justify-between">
      <div className="flex items-center gap-2">
        <File size={24} color="#0B3857" className="self-start" />
        <p className="flex flex-col">
          {file.name}
          <span className="text-grey-07 text-xs">{sizeKB} KB</span>
        </p>
      </div>
      {onRemove && (
        <button onClick={onRemove} type="button">
          <Trash size={24} color="#0B3857" />
        </button>
      )}
    </section>
  );
};

export default UploadedDocumentItem;
