import { type FC, useState, useMemo, type ReactNode } from "react";
import { Controller, useFormContext } from "react-hook-form";
import Icon from "../Icon";

export type PasswordRule = {
  id: string;
  label: string;
  test: (value: string) => boolean;
};

interface PasswordInputProps {
  id: string;
  name: string;
  label: string;
  placeholder?: string;
  rules?: PasswordRule[];
  match?: string;
  error?: string;
  helperText?: ReactNode;
}

const PasswordInput: FC<PasswordInputProps> = ({
  name,
  label,
  placeholder,
  rules = [],
  error,
  match,
  helperText,
}) => {
  const {
    control,
    formState: { errors, submitCount },
    watch,
  } = useFormContext<{ [key: string]: string }>();
  const [showPassword, setShowPassword] = useState(false);

  const value = watch(name) || "";
  const allRulesPassed = useMemo(
    () => rules.every((rule) => rule.test(value)),
    [rules, value]
  );
  const ruleColors = useMemo(
    () =>
      rules.map((rule) => {
        if (!value) return submitCount > 0 ? "text-red-04" : "text-grey-07";
        if (allRulesPassed) {
          return "text-grey-07";
        }
        return rule.test(value) ? "text-green-500" : "text-red-04";
      }),
    [rules, value, submitCount, allRulesPassed]
  );

  const getHelperColor = () => {
    if (errors[name]) return "text-red-04";

    if (!value) return "text-grey-07";
    if (match !== undefined)
      return value === match ? "text-green-500" : "text-red-04";
    return "text-grey-07";
  };
  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <div className="w-full">
          <label
            htmlFor={field.name}
            className="mb-1 font-bold text-[13px] text-blue-09"
          >
            {label}
          </label>
          <div className="relative">
            <input
              {...field}
              id={field.name}
              value={field.value ?? ""}
              type={showPassword ? "text" : "password"}
              placeholder={placeholder}
              autoComplete="new-password"
              className={`w-full p-4 pr-12 rounded-[8px] border border-solid placeholder:text-grey-05 focus:outline-none focus:ring-1 focus:ring-blue-05 ${
                errors[name] ? "border-red-04" : "border-grey-05"
              }`}
            />
            {field.value && (
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-blue-05 z-20"
              >
                <Icon
                  width={24}
                  height={24}
                  name={
                    showPassword ? "icon-hide-password" : "icon-show-password"
                  }
                />
              </button>
            )}
          </div>

          {rules.length > 0 && (
            <ul className="mt-1 text-xs list-none">
              {rules.map((rule, idx) => (
                <li
                  key={rule.id}
                  className={`relative pl-4 mb-1 ${ruleColors[idx]}`}
                >
                  <span className="absolute left-0 top-1/2 -translate-y-1/2 w-2 h-2 rounded-full bg-current"></span>
                  {rule.label}
                </li>
              ))}
            </ul>
          )}

          {error && <p className="mt-1 text-xs text-red-04">{error}</p>}
          {helperText && (
            <ul className="mt-1 text-xs list-none">
              <li className={`relative pl-4 mb-1 ${getHelperColor()}`}>
                <span className="absolute left-0 top-1/2 -translate-y-1/2 w-2 h-2 rounded-full bg-current"></span>
                {helperText}
              </li>
            </ul>
          )}
        </div>
      )}
    />
  );
};

export default PasswordInput;
