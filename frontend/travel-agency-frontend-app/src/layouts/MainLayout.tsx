import { Outlet } from "react-router-dom";
import Header from "../components/Header";
import { Toaster } from "react-hot-toast";
import { MessageCircleMore } from "lucide-react";
import { useState } from "react";
import AssistantWindow from "../components/AssistantWindow";

const MainLayout = () => {
  const [aiChatOpen, setAiChatOpen] = useState(false);
  return (
    <section className="relative min-h-screen">
      <Header />
      <div className="px-10 max-w-desktop mx-auto pb-10">
        <Outlet />
      </div>
      <Toaster position="top-right" />
      {aiChatOpen && <AssistantWindow />}
      <button
        className="p-6 cursor-pointer bg-white rounded-full fixed bottom-8 right-10 shadow-[0px_2px_10px_6px_#027EAC33] "
        onClick={() => setAiChatOpen(!aiChatOpen)}
      >
        <MessageCircleMore size={36} className="text-blue-05 " />
      </button>
    </section>
  );
};

export default MainLayout;
