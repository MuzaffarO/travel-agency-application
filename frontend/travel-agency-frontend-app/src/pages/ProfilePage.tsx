import { useEffect, useRef, useState } from "react";
import { User, Camera, Pencil, Check, X } from "lucide-react";
import { useForm, FormProvider } from "react-hook-form";
import { useDispatch } from "react-redux";
import Input from "../ui/Input";
import Button from "../components/Button";
import { getUserInfo, type UserInfo } from "../services/getUserInfo";
import { updateUserName, type UpdateUserNameRequest } from "../services/updateUserName";
import { updateUserImage } from "../services/updateUserImage";
import { setUserImageUrl } from "../store/user/userSlice";

type UserData = {
  firstName: string;
  lastName: string;
  imageUrl?: string;
};

export default function ProfilePage() {
  const dispatch = useDispatch();
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [imageError, setImageError] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const methods = useForm<UserData>({
    defaultValues: { firstName: "", lastName: "" },
    mode: "onSubmit",
  });

  const fetchUser = async () => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) {
      setError("User not found");
      setLoading(false);
      return;
    }

    try {
      const { token, email } = JSON.parse(savedUser);
      const data = await getUserInfo(token, email);
      setUserData({
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        imageUrl: data.imageUrl,
      });
      setImageError(false); // Reset image error when fetching new data
      methods.reset({
        firstName: data.firstName || "",
        lastName: data.lastName || "",
      });
    } catch (err: unknown) {
      console.error("Failed to fetch user data:", err);
      if (err instanceof Error) {
        setError(err.message || "Failed to load user data");
      } else {
        setError("Failed to load user data");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, [methods]);

  const onSubmit = async (data: Pick<UserData, "firstName" | "lastName">) => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) {
      setError("User not found");
      return;
    }

    const { token, email } = JSON.parse(savedUser);
    setError(null);
    setSuccess(null);

    try {
      const updateData: UpdateUserNameRequest = {};
      if (data.firstName && data.firstName.trim()) {
        updateData.firstName = data.firstName.trim();
      }
      if (data.lastName && data.lastName.trim()) {
        updateData.lastName = data.lastName.trim();
      }

      if (!updateData.firstName && !updateData.lastName) {
        setError("At least one of first name or last name is required");
        return;
      }

      await updateUserName(token, email, updateData);
      setSuccess("Your account has been updated successfully");
      setIsEditing(false);
      await fetchUser();
    } catch (err: unknown) {
      console.error("Failed to update user data:", err);
      if (err instanceof Error) {
        setError(err.message || "Failed to update user data");
      } else {
        setError("Failed to update user data");
      }
    }
  };

  const handleImageChange = async (file: File) => {
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith("image/")) {
      setError("Please select an image file");
      return;
    }

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      setError("Image size must be less than 5MB");
      return;
    }

    const savedUser = localStorage.getItem("user");
    if (!savedUser) {
      setError("User not found");
      return;
    }

    const { token, email } = JSON.parse(savedUser);
    setError(null);
    setSuccess(null);
    setUploadingImage(true);

    try {
      // Convert file to base64 using Promise
      const base64 = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          const result = reader.result?.toString();
          if (!result) {
            reject(new Error("Failed to read image file"));
            return;
          }
          // Remove "data:image/*;base64," prefix if present
          const base64String = result.includes(",") ? result.split(",")[1] : result;
          resolve(base64String);
        };
        reader.onerror = () => {
          reject(new Error("Failed to read image file"));
        };
        reader.readAsDataURL(file);
      });

      const response = await updateUserImage(token, email, base64);
      setSuccess("Profile picture updated successfully");
      setUserData((prev) => (prev ? { ...prev, imageUrl: response.imageUrl } : null));
      setImageError(false); // Reset image error when new image is uploaded
      // Update Redux store with new image URL immediately
      dispatch(setUserImageUrl(response.imageUrl));
      // Fetch user info to ensure everything is in sync
      await fetchUser();
    } catch (err: unknown) {
      console.error("Failed to update profile image:", err);
      if (err instanceof Error) {
        setError(err.message || "Failed to update profile image");
      } else {
        setError("Failed to update profile image");
      }
    } finally {
      setUploadingImage(false);
    }
  };

  if (loading) {
    return (
      <div className="bg-white w-full rounded-lg shadow p-6 max-w-xl">
        <div className="text-center text-gray-500">Loading...</div>
      </div>
    );
  }

  if (!userData) {
    return (
      <div className="bg-white w-full rounded-lg shadow p-6 max-w-xl">
        <div className="text-center text-red-600">{error || "Failed to load user data"}</div>
      </div>
    );
  }

  return (
    <div className="bg-white w-full rounded-lg shadow p-6 max-w-xl">
      <div className="flex items-center justify-between pb-4">
        <h3 className="h2 text-blue-09">General information</h3>
        {!isEditing && (
          <button
            onClick={() => setIsEditing(true)}
            className="text-blue-09 hover:text-blue-07 transition-colors">
            <Pencil className="w-5 h-5" />
          </button>
        )}
      </div>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {success && (
        <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded flex items-center gap-2">
          <Check className="w-4 h-4" />
          {success}
        </div>
      )}

      <div className="flex gap-6">
        <div className="relative self-start">
          {userData.imageUrl && !imageError ? (
            <img
              src={userData.imageUrl}
              alt="User avatar"
              className="w-32 h-32 rounded-full object-cover border-2 border-gray-200"
              onError={() => {
                setImageError(true);
              }}
            />
          ) : (
            <div className="w-32 h-32 rounded-full bg-gray-300 flex items-center justify-center border-2 border-gray-200">
              <User className="w-10 h-10 text-white" />
            </div>
          )}

          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploadingImage}
            className="absolute bottom-0 cursor-pointer right-0 text-blue-05 bg-white border-blue-05 border-2 p-1 rounded-full shadow hover:bg-blue-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            {uploadingImage ? (
              <div className="w-6 h-6 border-2 border-blue-05 border-t-transparent rounded-full animate-spin" />
            ) : (
              <Camera size={24} />
            )}
          </button>
          <input
            type="file"
            accept="image/png,image/jpeg,image/jpg,image/webp"
            className="hidden"
            ref={fileInputRef}
            onChange={(e) => {
              if (e.target.files && e.target.files[0]) {
                handleImageChange(e.target.files[0]);
              }
            }}
          />
        </div>

        <div className="space-y-2 flex-1">
          {isEditing ? (
            <FormProvider {...methods}>
              <form
                onSubmit={methods.handleSubmit(onSubmit)}
                className="space-y-4">
                <Input
                  {...methods.register("firstName", {
                    pattern: {
                      value: /^[A-Za-z'\-\s]{1,50}$/,
                      message: "Only Latin letters, hyphens, spaces, and apostrophes are allowed",
                    },
                    maxLength: {
                      value: 50,
                      message: "First name must be up to 50 characters",
                    },
                  })}
                  id="firstName"
                  name="firstName"
                  label="First name"
                  placeholder="Enter first name"
                  error={methods.formState.errors.firstName?.message}
                  helperText="John"
                />
                <Input
                  {...methods.register("lastName", {
                    pattern: {
                      value: /^[A-Za-z'\-\s]{1,50}$/,
                      message: "Only Latin letters, hyphens, spaces, and apostrophes are allowed",
                    },
                    maxLength: {
                      value: 50,
                      message: "Last name must be up to 50 characters",
                    },
                  })}
                  id="lastName"
                  name="lastName"
                  label="Last name"
                  placeholder="Enter last name"
                  error={methods.formState.errors.lastName?.message}
                  helperText="Doe"
                />

                <div className="flex justify-end gap-3 pt-2">
                  <Button
                    type="button"
                    variant="secondary"
                    size="medium"
                    onClick={() => {
                      setIsEditing(false);
                      setError(null);
                      setSuccess(null);
                      methods.reset(userData);
                    }}>
                    Cancel
                  </Button>
                  <Button type="submit" variant="primary" size="medium">
                    Save changes
                  </Button>
                </div>
              </form>
            </FormProvider>
          ) : (
            <>
              <div className="flex gap-3">
                <span className="body-bold text-blue-09">First name</span>
                <span className="body text-blue-09">{userData.firstName || "-"}</span>
              </div>
              <div className="flex gap-3">
                <span className="body-bold text-blue-09">Last name</span>
                <span className="body text-blue-09">{userData.lastName || "-"}</span>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
