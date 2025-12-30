import { useEffect, useState } from "react";
import Button from "../components/Button";
import { listTravelAgents, type TravelAgent } from "../services/listTravelAgents";
import { deleteTravelAgent } from "../services/deleteTravelAgent";
import CreateTravelAgentModal from "../components/modals/CreateTravelAgentModal/CreateTravelAgentModal";
import { useNavigate } from "react-router-dom";
import { Trash2, Plus, User } from "lucide-react";

const AdminTravelAgentsPage = () => {
  const [agents, setAgents] = useState<TravelAgent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [agentForDelete, setAgentForDelete] = useState<TravelAgent | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const navigate = useNavigate();

  const fetchAgents = async () => {
    setLoading(true);
    setError(null);
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        setError("User not found");
        setLoading(false);
        navigate("/login");
        return;
      }

      const user = JSON.parse(storedUser) as { token?: string };
      const token = user.token;

      if (!token) {
        setError("Token not found");
        setLoading(false);
        return;
      }

      const data = await listTravelAgents(token);
      setAgents(data);
    } catch (err: unknown) {
      if (err instanceof Error) {
        console.error("Failed to fetch travel agents", err);
        setError(err.message || "Failed to fetch travel agents");
      } else {
        console.error("Failed to fetch travel agents", err);
        setError("Failed to fetch travel agents");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAgents();
  }, []);

  const handleDelete = async (agent: TravelAgent) => {
    if (!window.confirm(`Are you sure you want to delete ${agent.firstName} ${agent.lastName} (${agent.email})?`)) {
      return;
    }

    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) {
        setError("User not found");
        return;
      }

      const user = JSON.parse(storedUser) as { token?: string };
      const token = user.token;

      if (!token) {
        setError("Token not found");
        return;
      }

      await deleteTravelAgent(token, agent.email);
      await fetchAgents();
      setAgentForDelete(null);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message || "Failed to delete travel agent");
      } else {
        setError("Failed to delete travel agent");
      }
    }
  };

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
      });
    } catch {
      return dateString;
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center h-64">
          <div className="text-gray-500">Loading travel agents...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Travel Agents Management</h1>
        <Button onClick={() => setIsCreateModalOpen(true)}>
          <Plus className="w-4 h-4 mr-2" />
          Create Travel Agent
        </Button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {agents.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <User className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <p className="text-gray-500 text-lg">No travel agents found</p>
          <p className="text-gray-400 text-sm mt-2">Create your first travel agent to get started</p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Role
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created By
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Contact
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {agents.map((agent) => (
                <tr key={agent.email} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {agent.firstName} {agent.lastName}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-500">{agent.email}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      agent.role === "ADMIN" 
                        ? "bg-purple-100 text-purple-800" 
                        : "bg-blue-100 text-blue-800"
                    }`}>
                      {agent.role}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(agent.createdAt)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {agent.createdBy || "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div>{agent.phone || "-"}</div>
                    <div className="text-xs">{agent.messenger || ""}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => handleDelete(agent)}
                      className="text-red-600 hover:text-red-900 inline-flex items-center"
                      title="Delete agent"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <CreateTravelAgentModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={fetchAgents}
      />
    </div>
  );
};

export default AdminTravelAgentsPage;

