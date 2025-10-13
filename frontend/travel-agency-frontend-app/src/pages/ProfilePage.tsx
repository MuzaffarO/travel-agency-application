import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { User, Camera, Pencil } from "lucide-react";
import { useForm, FormProvider } from "react-hook-form";
import Input from "../ui/Input";
import Button from "../components/Button";
import { BACK_URL } from "../constants";

type UserData = {
  firstName: string;
  lastName: string;
  imageUrl?: string;
};

export default function ProfilePage() {
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const methods = useForm<UserData>({
    defaultValues: { firstName: "", lastName: "" },
    mode: "onSubmit",
  });

  useEffect(() => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) return;

    const { token, email } = JSON.parse(savedUser);

    const fetchUser = async () => {
      try {
        const res = await axios.get(
          `${BACK_URL}/users/${encodeURIComponent(email)}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        setUserData(res.data);
        methods.reset(res.data);
      } catch (err) {
        console.error("Failed to fetch user data:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [methods]);

  const onSubmit = async (data: Pick<UserData, "firstName" | "lastName">) => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) return;

    const { token, email } = JSON.parse(savedUser);

    try {
      await axios.put(
        `${BACK_URL}/users/${encodeURIComponent(email)}/name`,
        {
          firstName: data.firstName,
          lastName: data.lastName,
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setIsEditing(false);
      window.location.reload();
    } catch (err) {
      console.error("Failed to update user data:", err);
    }
  };

  const handleImageChange = async (file: File) => {
    if (!file) return;

    const savedUser = localStorage.getItem("user");
    if (!savedUser) return;

    const { token, email } = JSON.parse(savedUser);

    const reader = new FileReader();
    reader.onloadend = async () => {
      const base64 = reader.result?.toString().split(",")[1]; // remove "data:*/*;base64,"
      if (!base64) return;

      try {
        await axios.put(
          `${BACK_URL}/${encodeURIComponent(email)}/image`,
          { imageBase64: base64 },
          { headers: { Authorization: `Bearer ${token}` } }
        );
        window.location.reload();
      } catch (err) {
        console.error("Failed to update profile image:", err);
      }
    };

    reader.readAsDataURL(file);
  };

  if (loading) return <div className="text-blue-09">Loading...</div>;
  if (!userData)
    return <div className="text-red-04">Failed to load user data</div>;

  return (
    <div className="bg-white w-full rounded-lg shadow p-6 max-w-xl">
      <div className="flex items-center justify-between pb-4">
        <h3 className="h2 text-blue-09">General information</h3>
        {!isEditing && (
          <button
            onClick={() => setIsEditing(true)}
            className="text-blue-09 hover:text-blue-07">
            <Pencil className="w-5 h-5" />
          </button>
        )}
      </div>

      <div className="flex gap-6">
        <div className="relative self-start">
          {userData.imageUrl ? (
            <img
              src={userData.imageUrl}
              alt="User avatar"
              className="w-30 h-30 rounded-full object-cover"
            />
          ) : (
            <div className="w-30 h-30 rounded-full bg-gray-300 flex items-center justify-center">
              <User className="w-10 h-10 text-white" />
            </div>
          )}

          <button
            onClick={() => fileInputRef.current?.click()}
            className="absolute bottom-0 cursor-pointer right-0 text-blue-05 bg-white border-blue-05 border-2 p-1 rounded-full shadow">
            <Camera size={24} />
          </button>
          <input
            type="file"
            accept="image/*"
            className="hidden"
            ref={fileInputRef}
            onChange={(e) =>
              e.target.files && handleImageChange(e.target.files[0])
            }
          />
        </div>

        <div className="space-y-2 flex-1">
          {isEditing ? (
            <FormProvider {...methods}>
              <form
                onSubmit={methods.handleSubmit(onSubmit)}
                className="space-y-4">
                <Input
                  {...methods.register("firstName")}
                  id="firstName"
                  name="firstName"
                  label="First name"
                  placeholder="Enter first name"
                  error={methods.formState.errors.firstName?.message}
                  helperText="John"
                />
                <Input
                  {...methods.register("lastName")}
                  id="lastName"
                  name="lastName"
                  label="Last name"
                  placeholder="Enter last name"
                  error={methods.formState.errors.lastName?.message}
                  helperText="Doe"
                />

                <div className="flex justify-end gap-3 pt-2">
                  <Button
                    variant="secondary"
                    size="medium"
                    onClick={() => setIsEditing(false)}>
                    Cancel
                  </Button>
                  <Button variant="primary" size="medium">
                    Save changes
                  </Button>
                </div>
              </form>
            </FormProvider>
          ) : (
            <>
              <div className="flex gap-3">
                <span className="body-bold text-blue-09">First name</span>
                <span className="body text-blue-09">{userData.firstName}</span>
              </div>
              <div className="flex gap-3">
                <span className="body-bold text-blue-09">Last name</span>
                <span className="body text-blue-09">{userData.lastName}</span>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
