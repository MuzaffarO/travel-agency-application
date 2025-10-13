import Select, {
  components,
  type DropdownIndicatorProps,
  type MultiValue,
  type SingleValue,
  type StylesConfig,
} from "react-select";
import Icon from "../../ui/Icon";
import { useEffect, useState } from "react";
import { fetchLocations } from "../../services/fetchLocations.ts";
import useDebounce from "../../hooks/useDebounce.ts";
import { useAppDispatch } from "../../store/hooks/useAppDispatch.ts";
import { useAppSelector } from "../../store/hooks/useAppSelector.ts";
import { setSelectedLocation } from "../../store/location/locationSlice.ts";

type OptionType = { value: string; label: string };

const customStyles: StylesConfig<OptionType> = {
  control: (provided) => ({
    ...provided,
    display: "flex",
    alignItems: "center",
    fontSize: "14px",
    borderRadius: "8px",
    border: "1px solid var(--color-grey-05)",
    boxShadow: "none",
    paddingLeft: "33px",
    position: "relative",
    "&:hover": {
      border: "1px solid #0077B6",
    },
    height: "56px",
  }),
  placeholder: (provided) => ({
    ...provided,
    color: "var(--color-blue-09)",
  }),
  singleValue: (provided) => ({
    ...provided,
    color: "#333333",
    display: "flex",
    alignItems: "center",
  }),
  menu: (provided) => ({
    ...provided,
    borderWidth: "1px",
    borderColor: "#E6EAF2",
    borderRadius: "8px",
    boxShadow: "0px 4px 8px rgba(0, 0, 0, 0.1)",
    marginTop: "4px",
    zIndex: 1000,
  }),
  option: (provided, state) => ({
    ...provided,
    backgroundColor: state.isFocused ? "#E6F2FB" : "transparent",
    color: "#333333",
    padding: "8px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700",
  }),
};

const DropdownIndicator = <
  OptionType extends { value: string; label: string } = {
    value: string;
    label: string;
  },
  IsMulti extends boolean = false,
>(
  props: DropdownIndicatorProps<OptionType, IsMulti>
) => {
  const { selectProps } = props;

  return (
    <components.DropdownIndicator {...props}>
      <div
        style={{
          display: "flex",
          transition: "transform 0.3s ease",
          transform: selectProps.menuIsOpen ? "rotate(180deg)" : "rotate(0deg)",
          paddingRight: "4px",
        }}
      >
        <Icon name="icon-chevron" />
      </div>
    </components.DropdownIndicator>
  );
};
type LocationDropdownProps = {
  noIcon?: boolean;
  customLabel?: string;
};
const LocationDropdown = ({
  noIcon = false,
  customLabel,
}: LocationDropdownProps) => {
  const dispatch = useAppDispatch();

  const selectedLocation = useAppSelector(
    (state) => state.location.selectedLocation
  );

  const [inputValue, setInputValue] = useState("");
  const [options, setOptions] = useState<OptionType[]>([]);

  const debouncedInput = useDebounce(inputValue, 500);

  useEffect(() => {
    const fetchOptions = async () => {
      if (!debouncedInput || debouncedInput.length < 3) {
        setOptions([]);
        return;
      }

      try {
        const locations = await fetchLocations(debouncedInput);
        const formattedOptions = locations.map((location) => ({
          value: location,
          label: location,
        }));

        setOptions(formattedOptions);
      } catch (error) {
        console.error("Failed to fetch locations:", error);
      }
    };

    fetchOptions();
  }, [debouncedInput]);

  const handleInputChange = (newValue: string) => {
    setInputValue(newValue);
  };

  const isSingleValue = (
    value: SingleValue<OptionType> | MultiValue<OptionType>
  ): value is SingleValue<OptionType> => !!value && !Array.isArray(value);

  const handleChange = (
    newValue: SingleValue<OptionType> | MultiValue<OptionType>
  ) => {
    if (isSingleValue(newValue)) {
      dispatch(setSelectedLocation(newValue!.value));
    } else {
      dispatch(setSelectedLocation(null));
    }
  };

  return (
    <div className="relative">
      {!noIcon && (
        <Icon name="icon-location" className="absolute top-4 left-3 z-10" />
      )}{" "}
      <Select
        value={
          selectedLocation
            ? { value: selectedLocation, label: selectedLocation }
            : null
        }
        options={options}
        styles={{
          ...customStyles,
          control: (provided, state) => ({
            ...customStyles.control!(provided, state),
            paddingLeft: noIcon ? "4px" : "33px",
          }),
        }}
        components={{
          IndicatorSeparator: () => null,
          DropdownIndicator: DropdownIndicator,
        }}
        placeholder={customLabel || "Any destination"}
        onInputChange={handleInputChange}
        onChange={handleChange}
        inputValue={inputValue}
        isClearable
        noOptionsMessage={() =>
          inputValue.length < 3
            ? "Type at least 3 characters"
            : "No options available"
        }
      />
    </div>
  );
};

export default LocationDropdown;
