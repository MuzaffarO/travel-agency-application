import { useDispatch, useSelector } from "react-redux";
import Logo from "../../../ui/Logo";
import Modal from "../../../ui/Modal";
import Button from "../../Button";
import type { RootState } from "../../../store/store";
import { closeModal } from "../../../store/modal/modalSlice";
import { useNavigate } from "react-router-dom";
import {useAppSelector} from "../../../store/hooks/useAppSelector.ts";

const IsNotLoggedModal = () => {
  const dispatch = useDispatch();
  const activeModal = useSelector(
    (state: RootState) => state.modal.activeModal
  );
  const isAuth = useAppSelector(state => state.user.isAuth);

  const navigate = useNavigate();

  const isOpen = activeModal === "IsNotLogged" && !isAuth;

  const handleClose = () => {
    dispatch(closeModal());
  };
  return (
    <Modal onClose={handleClose} isOpen={isOpen} className="md:w-[544px]">
      <div className="p-6 flex flex-col gap-4 ">
        <Logo color="#027EAC" className="self-start" />
        <p className="text-[18px] font-bold mb-4 self-start">
          To book a tour please sign in or create an account
        </p>
        <Button onClick={() => navigate("/login")}>Sign in</Button>
        <Button variant="secondary" onClick={() => navigate("/register")}>
          Create an account
        </Button>
      </div>
    </Modal>
  );
};

export default IsNotLoggedModal;
