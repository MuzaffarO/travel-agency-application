import { useState, useEffect, useRef, useMemo } from "react";
import { SendHorizontal, Bot } from "lucide-react";
import axios from "axios";
import { BACK_URL } from "../../constants";
import Textarea from "../../ui/Textarea";
import TypingDots from "../../ui/TypingDots";

const AssistantWindow = () => {
  const [userInput, setUserInput] = useState("");
  const [chatMessages, setChatMessages] = useState<
    { from: "user" | "ai"; message: string }[]
  >([]);
  const [loading, setLoading] = useState(false);

  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const initialAIPrompt = useMemo(
    () => ({
      from: "ai" as const,
      message:
        "Hello! I'm your TourAI Assistant. I can quickly answer all questions about tours, prices, and booking.\n\nType your question below.",
    }),
    []
  );

  useEffect(() => {
    if (chatMessages.length === 0) {
      setChatMessages([initialAIPrompt]);
    }
  }, [chatMessages.length, initialAIPrompt]);

  const handleSend = async () => {
    if (!userInput.trim()) return;

    setChatMessages((prev) => [...prev, { from: "user", message: userInput }]);
    setLoading(true);

    try {
      const storedUser = localStorage.getItem("user");
      const token = storedUser ? JSON.parse(storedUser).token : null;

      const response = await axios.post(
        `${BACK_URL}/ai/chat`,
        { message: userInput },
        {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        }
      );

      const aiMessageText = response.data?.reply ?? "No response from AI";
      setChatMessages((prev) => [
        ...prev,
        { from: "ai", message: aiMessageText },
      ]);
    } catch (error) {
      console.error(error);
      setChatMessages((prev) => [
        ...prev,
        {
          from: "ai",
          message: "Sorry, something went wrong. Please try again.",
        },
      ]);
    } finally {
      setUserInput("");
      setLoading(false);

      if (textareaRef.current) {
        textareaRef.current.style.height = "auto";
      }
    }
  };

  return (
    <section className="w-[450px] h-[580px] bg-white fixed bottom-32 right-10 rounded-lg shadow-[0px_2px_10px_6px_#027EAC33] p-6 flex flex-col gap-4 z-1000">
      <h2 className="text-2xl text-blue-05 font-bold">TourAI Assistant</h2>
      <div className="border-t-2 overflow-y-auto h-full border-grey-05 flex flex-col justify-between pt-4">
        <div className="overflow-y-auto flex-1 flex flex-col gap-2 mb-2 scrollbar-thin scrollbar-thumb-rounded scrollbar-thumb-blue-500 [scrollbar-gutter:stable]">
          {chatMessages.map((msg, index) => (
            <div
              key={index}
              className={`flex items-start break-words ${msg.from === "user" ? "self-end justify-end" : "self-start justify-start"}`}
            >
              {msg.from === "ai" && (
                <div className="mr-2 mt-1">
                  <Bot className="w-5 h-5 text-gray-500" />
                </div>
              )}
              <div
                className={`text-sm p-3 rounded-lg whitespace-pre-line break-words max-w-[80%] inline-block ${
                  msg.from === "user"
                    ? "bg-blue-500 text-white rounded-br-none"
                    : "bg-gray-100 text-gray-900 rounded-bl-none"
                }`}
              >
                {msg.message}
              </div>
            </div>
          ))}

          {loading && (
            <div className="flex items-start self-start">
              <div className="mr-2 mt-1">
                <Bot className="w-5 h-5 text-gray-500" />
              </div>
              <TypingDots />
            </div>
          )}
        </div>
      </div>
      <div className="flex flex-col gap-2 items-center w-full">
        <Textarea
          ref={textareaRef}
          placeholder="Type your question..."
          id="user-question"
          name="user-question"
          label=""
          icon={<SendHorizontal />}
          value={userInput}
          onChange={(e) => setUserInput(e.target.value)}
          onIconClick={handleSend}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
        />
      </div>
    </section>
  );
};

export default AssistantWindow;
