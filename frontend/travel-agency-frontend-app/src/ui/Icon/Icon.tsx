import iconsSpriteUrl from "../../assets/sprites.svg";
import type IconProps from "../../models/IconProps";

function Icon({
  name,
  className = "",
  width = 24,
  height = 24,
  onClick,
}: IconProps) {
  return (
    <span className="list-none cursor-pointer aspect-square w-fit">
      <svg
        className={`inline-block fill-current  ${className}`}
        width={width}
        height={height}
        onClick={onClick}
      >
        <use href={`${iconsSpriteUrl}#${name}`} />
      </svg>
    </span>
  );
}

export default Icon;
