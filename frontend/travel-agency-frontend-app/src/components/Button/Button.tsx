type ButtonVariant = "primary" | "secondary" | "white";
type ButtonSize = "small" | "medium" | "large";

type ButtonProps = {
  children?: React.ReactNode;
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
};

const Button: React.FC<ButtonProps> = ({
  children = "Button",
  onClick,
  disabled = false,
  className = "",
  variant = "primary",
  size = "medium",
}) => {
  const getVariantClasses = () => {
    switch (variant) {
      case "primary":
        return "bg-[#027EAC] hover:bg-[#015878] text-white border-0";
      case "secondary":
        return "bg-white hover:border-[#015878] hover:text-[#015878] text-[#027EAC] border-2 border-[#027EAC]";
      case "white":
        return "bg-white hover:text-white hover:bg-[#027EAC] text-[#027EAC] border-0 border-[#027EAC]";
      default:
        return "bg-[#027EAC] hover:bg-[#015878] text-white border-0";
    }
  };

  const getSizeClasses = () => {
    switch (size) {
      case "small":
        return "px-2 py-1";
      case "medium":
        return "px-4 py-2";
      case "large":
        return "py-4 px-4";
    }
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`${getVariantClasses()} ${getSizeClasses()} disabled:opacity-50 disabled:cursor-not-allowed duration-200 cursor-pointer text-sm font-bold rounded-lg ${className}`}>
      {children}
    </button>
  );
};

export default Button;
