import { type TextareaHTMLAttributes, forwardRef } from "react";

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  icon?: React.ReactNode;
  onIconClick?: () => void;
}

const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, icon, onIconClick, ...props }, ref) => {
    return (
      <div className="w-full relative">
        {label && <label className="block text-sm mb-1">{label}</label>}
        <div className="relative">
          <textarea
            ref={ref}
            {...props}
            className="border-grey-05 w-full p-4 pr-12 rounded-[8px] border border-solid placeholder:text-grey-06 
                       focus:outline-none focus:ring-1 focus:ring-blue-05 
                       hover:shadow-[0px_2px_10px_6px_#027EAC33] transition-shadow duration-200 
                       overflow-hidden resize-none"
            rows={1}
            onInput={(e) => {
              e.currentTarget.style.height = "auto";
              e.currentTarget.style.height = `${e.currentTarget.scrollHeight}px`;
            }}
          />
          {icon && (
            <span
              onClick={onIconClick}
              className="absolute right-4 bottom-6 text-blue-09 cursor-pointer"
            >
              {icon}
            </span>
          )}
        </div>
      </div>
    );
  }
);

export default Textarea;
