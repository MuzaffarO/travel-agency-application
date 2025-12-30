import { useEffect, useState } from "react";
import Button from "../components/Button";
import { getMyTours } from "../services/getMyTours";
import { deleteTour } from "../services/deleteTour";
import CreateTourModal from "../components/modals/CreateTourModal/CreateTourModal";
import EditTourModal from "../components/modals/EditTourModalForTours/EditTourModalForTours";
import type { Tour } from "./MainPage";
import { useNavigate } from "react-router-dom";
import defaultTourImage from "../assets/default-tour.png";
import { Trash2, Edit, Plus, Eye } from "lucide-react";

const TravelAgentToursPage = () => {
  const [tours, setTours] = useState<Tour[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tourForEdit, setTourForEdit] = useState<Tour | null>(null);
  const [tourForDelete, setTourForDelete] = useState<Tour | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const navigate = useNavigate();

  const fetchTours = async () => {
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

      const data = await getMyTours(token);
      setTours(data);
    } catch (err: unknown) {
      if (err instanceof Error) {
        console.error("Failed to fetch tours", err);
        setError(err.message || "Failed to fetch tours");
      } else {
        console.error("Failed to fetch tours", err);
        setError("Failed to fetch tours");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTours();
  }, []);

  const handleDelete = async (tour: Tour) => {
    try {
      const storedUser = localStorage.getItem("user");
      if (!storedUser) return;

      const user = JSON.parse(storedUser) as { token?: string };
      const token = user.token;

      if (!token) return;

      await deleteTour(tour.id, token);
      setTours((prev) => prev.filter((t) => t.id !== tour.id));
      setTourForDelete(null);
    } catch (err) {
      console.error("Failed to delete tour", err);
      alert("Failed to delete tour. Please try again.");
    }
  };

  if (loading) return <div className="pt-10">Loading...</div>;
  if (error) return <p className="pt-10">Error: {error}</p>;

  return (
    <div className="pt-10">
      <div className="flex justify-between items-center mb-6">
        <h1 className="h1">My Tours</h1>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => navigate("/")}>
            Browse All Tours
          </Button>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            <Plus size={16} className="mr-2" />
            Create Tour
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {tours.length > 0 ? (
          tours.map((tour) => (
            <div
              key={tour.id}
              className="rounded-xl bg-white shadow-card p-6 flex flex-col gap-4 hover:shadow-lg transition-shadow"
            >
              <div 
                className="cursor-pointer"
                onClick={() => navigate(`/tours/${tour.id}`)}
              >
                <img
                  className="w-full h-48 object-cover rounded-xl"
                  src={tour.imageUrl || defaultTourImage}
                  alt={tour.name}
                />
                <div className="mt-4">
                  <h3 className="h3 mb-2 hover:text-blue-09 transition-colors">{tour.name}</h3>
                  <p className="body text-blue-09 mb-2">{tour.destination}</p>
                  <p className="caption text-[#677883]">
                    {tour.rating > 0 ? `⭐ ${tour.rating} (${tour.reviews} reviews)` : "No reviews yet"}
                  </p>
                </div>
              </div>
              <div className="mt-auto flex flex-col gap-2">
                <Button
                  variant="secondary"
                  onClick={() => navigate(`/tours/${tour.id}`)}
                  className="w-full"
                >
                  <Eye size={16} className="mr-2" />
                  View Details
                </Button>
                <div className="flex gap-2">
                  <Button
                    variant="secondary"
                    onClick={() => setTourForEdit(tour)}
                    className="flex-1"
                  >
                    <Edit size={16} className="mr-2" />
                    Edit
                  </Button>
                  <Button
                    variant="secondary"
                    onClick={() => {
                      if (window.confirm(`Are you sure you want to delete "${tour.name}"?`)) {
                        handleDelete(tour);
                      }
                    }}
                    className="flex-1"
                  >
                    <Trash2 size={16} className="mr-2" />
                    Delete
                  </Button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <p className="text-3xl flex items-center justify-center col-start-1 col-end-4 h-80 text-blue-09">
            No tours yet. Create your first tour!
          </p>
        )}
      </div>

      {isCreateModalOpen && (
        <CreateTourModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={() => {
            fetchTours();
            setIsCreateModalOpen(false);
          }}
        />
      )}

      {tourForEdit && (
        <EditTourModal
          tour={tourForEdit}
          isOpen={!!tourForEdit}
          onClose={() => setTourForEdit(null)}
          onSuccess={() => {
            fetchTours();
            setTourForEdit(null);
          }}
        />
      )}
    </div>
  );
};

export default TravelAgentToursPage;

