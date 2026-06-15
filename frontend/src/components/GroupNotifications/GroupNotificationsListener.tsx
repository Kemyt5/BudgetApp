import { useEffect } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import { Client } from "@stomp/stompjs";

interface GroupNotification {
  type: "GROUP_EXPENSE_ADDED";
  groupId: number | string;
  groupName: string;
  title: string;
  amount: number;
  userShare: number;
  createdByEmail: string;
  message: string;
}

const getWebSocketUrl = (token: string) => {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  return `${protocol}://localhost:8080/ws/group-notifications?token=${encodeURIComponent(token)}`;
};

const GroupNotificationsListener = () => {
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthenticated) return;

    const token = localStorage.getItem("accessToken");
    if (!token) return;

    // Tworzymy klienta STOMP
    const client = new Client({
      brokerURL: getWebSocketUrl(token),
      onConnect: () => {
        console.log("Połączono z WebSocket (STOMP)");
        // Subskrybujemy kanał prywatny użytkownika
        client.subscribe("/user/queue/notifications", (message) => {
          try {
            const notification = JSON.parse(message.body) as GroupNotification;
            if (notification.type === "GROUP_EXPENSE_ADDED") {
              toast.info(notification.message);
            }
          } catch (error) {
            console.error("Błąd parsowania powiadomienia:", error);
          }
        });
      },
      onStompError: (frame) => {
        console.error("Błąd STOMP:", frame.headers["message"]);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [isAuthenticated]);

  return null;
};

export default GroupNotificationsListener;