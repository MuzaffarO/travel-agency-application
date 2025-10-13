import { toast, type Toast } from "react-hot-toast";
import Icon from "../../ui/Icon";

type SuccessToastProps = {
  t: Toast;
  title: string;
  message: string;
};

const SuccessToast = ({ t, title, message }: SuccessToastProps) => {
  return (
    <div
      className={`${
        t.visible ? "animate-enter" : "animate-leave"
      } max-w-md w-full bg-green-01 shadow-lg rounded-lg pointer-events-auto flex ring-1 ring-green-04 ring-opacity-5 border border-green-04 text-blue-09 p-4 items-start gap-4`}
    >
      <svg
        className="h-6 w-6 flex-shrink-0 text-green-04 mt-0.5"
        fill="currentColor"
        viewBox="0 0 24 24"
        stroke="#FFFFFF"
        strokeWidth="2"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>

      <div className="flex-1 text-blue-09">
        <p className="text-lg font-bold">{title}</p>
        <p className="mt-1 text-sm">{message}</p>
      </div>

      <button
        onClick={() => toast.dismiss(t.id)}
        className="flex-shrink-0 text-gray-400 hover:text-gray-500 transition duration-150 ease-in-out mt-0.5"
      >
        <Icon name="icon-close" className="h-5 w-5 stroke-current" />
      </button>
    </div>
  );
};

export default SuccessToast;
