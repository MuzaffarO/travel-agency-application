import Icon from "../Icon";

const Logo = ({ color = "#027EAC", className = "" }) => {
  return (
    <div
      className={`flex items-center justify-center gap-2.5 font-bold text-2xl cursor-pointer ${className}`}
      style={{ color: color }}
    >
      <Icon name="icon-logo" width={48} height={48} />
      <h2>Travel Agency</h2>
    </div>
  );
};

export default Logo;
