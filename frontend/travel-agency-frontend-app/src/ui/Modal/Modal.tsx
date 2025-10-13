import { type ReactNode } from "react";
import Icon from "../Icon";

type ModalProps = {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
  className?: string;
};

const Modal = ({ isOpen, onClose, className, children }: ModalProps) => {
  if (!isOpen) return null;

  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      onClick={handleOverlayClick}
      className={`fixed inset-0 bg-black/50 flex items-center justify-center z-50`}>
      <div
        className={`bg-white rounded-lg shadow-lg relative md:h-auto text-blue-09 ${className}`}>
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-600 hover:text-black">
          <Icon name="icon-close" width={24} height={24} />
        </button>

        {children}
      </div>
    </div>
  );
};

export default Modal;
