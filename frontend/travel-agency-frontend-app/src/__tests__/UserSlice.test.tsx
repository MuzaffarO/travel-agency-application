import { describe, it, expect, vi } from "vitest";
import reducer, { logout, restoreUser } from "../store/user/userSlice";
import { signIn, signUp } from "../store/user/userThunks";

describe("userSlice basic tests", () => {
  const initialState = {
    isAuth: false,
    firstName: "",
    lastName: "",
    email: "",
    token: "",
    role: "",
    userName: "",
  };

  it("should return the initial state", () => {
    expect(reducer(undefined, { type: "unknown" })).toEqual(initialState);
  });

  it("should handle logout", () => {
    const loggedInState = {
      ...initialState,
      isAuth: true,
      firstName: "Olena",
      lastName: "Hrynko",
      email: "olena@example.com",
    };
    const state = reducer(loggedInState, logout());
    expect(state).toEqual(initialState);
  });

  it("should restore user from localStorage", () => {
    const mockUser = {
      isAuth: true,
      firstName: "Olena",
      lastName: "Hrynko",
      email: "olena@example.com",
      token: "",
      role: "",
      userName: "",
    };
    Storage.prototype.getItem = vi.fn(() => JSON.stringify(mockUser));

    const state = reducer(initialState, restoreUser());
    expect(state).toEqual(mockUser);
  });

  it("should handle signUp.fulfilled", () => {
    const action = {
      type: signUp.fulfilled.type,
      payload: {
        firstName: "Olena",
        lastName: "Hrynko",
        email: "olena@example.com",
        id: "123",
      },
    };
    const state = reducer(initialState, action);
    expect(state.isAuth).toBe(true);
    expect(state.firstName).toBe("Olena");
    expect(state.lastName).toBe("Hrynko");
    expect(state.email).toBe("olena@example.com");
  });

  it("should handle signIn.fulfilled", () => {
    const action = {
      type: signIn.fulfilled.type,
      payload: {
        email: "olena@example.com",
        role: "user",
        userName: "olena123",
        idToken: "token123",
      },
    };
    const state = reducer(initialState, action);
    expect(state.isAuth).toBe(true);
    expect(state.email).toBe("olena@example.com");
    expect(state.role).toBe("user");
    expect(state.userName).toBe("olena123");
    expect(state.token).toBe("token123");
  });

  it("should keep state unchanged if localStorage is empty", () => {
    Storage.prototype.getItem = vi.fn(() => null);
    const state = reducer(initialState, restoreUser());
    expect(state).toEqual(initialState);
  });
  it("should remove user from localStorage on logout", () => {
    const removeItemMock = vi.spyOn(Storage.prototype, "removeItem");
    reducer(
      { ...initialState, isAuth: true, email: "olena@example.com" },
      logout()
    );
    expect(removeItemMock).toHaveBeenCalledWith("user");
    removeItemMock.mockRestore();
  });

  it("should keep state unchanged if localStorage has invalid JSON", () => {
    Storage.prototype.getItem = vi.fn(() => "{ invalid json ");
    const state = reducer(initialState, restoreUser());
    expect(state).toEqual(initialState);
  });

  it("should handle signUp.rejected without changing state", () => {
    const action = { type: signUp.rejected.type, payload: "Error" };
    const state = reducer(initialState, action);
    expect(state).toEqual(initialState);
  });

  it("should handle signIn.rejected without changing state", () => {
    const action = { type: signIn.rejected.type, payload: "Error" };
    const state = reducer(initialState, action);
    expect(state).toEqual(initialState);
  });

  it("should store correct data in localStorage on signIn.fulfilled", () => {
    const setItemMock = vi.spyOn(Storage.prototype, "setItem");
    const action = {
      type: signIn.fulfilled.type,
      payload: {
        email: "olena@example.com",
        role: "user",
        userName: "olena123",
        idToken: "token123",
      },
    };
    reducer(initialState, action);
    expect(setItemMock).toHaveBeenCalledWith(
      "user",
      JSON.stringify({
        isAuth: true,
        email: "olena@example.com",
        token: "token123",
        role: "user",
        userName: "olena123",
      })
    );
    setItemMock.mockRestore();
  });
});
