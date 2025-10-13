type Json = Record<string, any>;

export interface MockResponse {
  status(): number;
  json(): Promise<Json>;
}

function buildResponse(statusCode: number, body: Json): MockResponse {
  return {
    status: () => statusCode,
    json: async () => body,
  };
}

export function createMockApi() {
  return {
    async post(path: string, options?: { data?: Json; headers?: Json }) {
      if (path === "/auth/sign-up") {
        const email = options?.data?.email as string;
        if (email?.includes("exists")) {
          return buildResponse(409, { message: "Email already exists" });
        }
        return buildResponse(201, { message: "Account created successfully" });
      }
      if (path === "/auth/sign-in") {
        const { email, password } = (options?.data || {}) as Json;
        if (email && password && password !== "wrong-pass") {
          return buildResponse(200, {
            idToken: "mock-id-token",
            role: "CUSTOMER",
            userName: email,
            email,
          });
        }
        return buildResponse(400, { message: "Wrong password or email" });
      }
      if (path === "/bookings") {
        const { tourId, travelDate } = (options?.data || {}) as Json;
        if (!tourId || !travelDate)
          return buildResponse(400, { message: "Bad request" });
        // Accept future date, reject past
        const isFuture = new Date(travelDate).getTime() > Date.now();
        if (!isFuture)
          return buildResponse(400, {
            message: "Travel date cannot be in the past",
          });
        return buildResponse(201, {
          bookingId: "B-0001",
          status: "CREATED",
          confirmationNumber: "CONF-123",
        });
      }
      return buildResponse(404, { message: "Not Found" });
    },
    async get(path: string, options?: { headers?: Json }) {
      if (path.startsWith("/tours/available")) {
        return buildResponse(200, {
          page: 1,
          pageSize: 6,
          totalPages: 1,
          totalItems: 2,
          tours: [
            {
              id: "T-0001",
              title: "Paris City Break",
              destination: "Paris",
              price: 500,
            },
            {
              id: "T-0002",
              title: "Dominican Resort",
              destination: "Punta Cana",
              price: 1400,
            },
          ],
        });
      }
      if (path.startsWith("/tours/")) {
        const id = path.split("/").pop();
        if (id === "T-0001" || id === "T-0002") {
          return buildResponse(200, {
            id,
            title: "Mock Tour",
            destination: "Mock",
            price: 1000,
            duration: "7 days",
          });
        }
        return buildResponse(404, { message: "Tour not found" });
      }
      if (path === "/health") {
        return buildResponse(200, { status: "ok" });
      }
      return buildResponse(404, { message: "Not Found" });
    },
  };
}
