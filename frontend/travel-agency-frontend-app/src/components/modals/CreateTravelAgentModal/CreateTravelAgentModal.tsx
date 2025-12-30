import { useState } from "react";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import Input from "../../../ui/Input";
import { createTravelAgent, type CreateTravelAgentRequest } from "../../../services/createTravelAgent";

type CreateTravelAgentModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

const CreateTravelAgentModal: React.FC<CreateTravelAgentModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [formData, setFormData] = useState<CreateTravelAgentRequest>({
    email: "",
    firstName: "",
    lastName: "",
    role: "TRAVEL_AGENT",
    password: "",
    phone: "",
    messenger: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.email || !formData.firstName || !formData.lastName || !formData.password) {
      setError("Please fill in all required fields");
      return;
    }

    // Basic password validation
    if (formData.password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }

    setLoading(true);
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        setError("User not found");
        setLoading(false);
        return;
      }

      const user = JSON.parse(storedUser) as { token?: string };
      const token = user.token;

      if (!token) {
        setError("Token not found");
        setLoading(false);
        return;
      }

      await createTravelAgent(token, formData);
      
      // Reset form
      setFormData({
        email: "",
        firstName: "",
        lastName: "",
        role: "TRAVEL_AGENT",
        password: "",
        phone: "",
        messenger: "",
      });
      
      onSuccess?.();
      onClose();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message || "Failed to create travel agent");
      } else {
        setError("Failed to create travel agent");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="md:w-[700px] max-h-[90vh] overflow-y-auto">
      <div className="p-6">
        <h2 className="h2 mb-6">Create Travel Agent</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email <span className="text-red-500">*</span>
          </label>
          <Input
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            required
            placeholder="agent@example.com"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              First Name <span className="text-red-500">*</span>
            </label>
            <Input
              type="text"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Last Name <span className="text-red-500">*</span>
            </label>
            <Input
              type="text"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Role <span className="text-red-500">*</span>
          </label>
          <select
            value={formData.role}
            onChange={(e) => setFormData({ ...formData, role: e.target.value as "TRAVEL_AGENT" | "ADMIN" })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="TRAVEL_AGENT">Travel Agent</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Password <span className="text-red-500">*</span>
          </label>
          <Input
            type="password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            required
            placeholder="At least 8 characters"
          />
          <p className="mt-1 text-xs text-gray-500">
            Must contain uppercase, lowercase, number, and special character
          </p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Phone (Optional)
          </label>
          <Input
            type="tel"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            placeholder="+1234567890"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Messenger (Optional)
          </label>
          <Input
            type="text"
            value={formData.messenger}
            onChange={(e) => setFormData({ ...formData, messenger: e.target.value })}
            placeholder="Username or contact info"
          />
        </div>

          <div className="flex justify-end gap-3 pt-4">
            <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create Agent"}
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
};

export default CreateTravelAgentModal;

