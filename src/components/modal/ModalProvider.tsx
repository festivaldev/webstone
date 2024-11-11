import { useDisclosure } from '@nextui-org/react';
import { createContext, useContext, useEffect, type ReactNode } from 'react';
import { createStore, useStore } from 'zustand';
import { useShallow } from 'zustand/shallow';
import { useSocketListeners } from '../SocketProvider';
import ChangeBlockGroupModal from './ChangeBlockGroupModal';
import ChangeBlockOrderModal from './ChangeBlockOrderModal';
import ChangeGroupOrderModal from './ChangeGroupOrderModal';
import CreateGroupModal from './CreateGroupModal';
import DeleteBlockModal from './DeleteBlockModal';
import DeleteGroupModal from './DeleteGroupModal';
import RenameBlockModal from './RenameBlockModal';
import RenameGroupModal from './RenameGroupModal';

interface ModalStateType {
  blockId: string | undefined;
  groupId: string | undefined;

  setBlockId: (blockId: string | undefined) => void;
  setGroupId: (groupId: string | undefined) => void;
}

interface ModalContextType {
  openRenameBlockModal: (blockId: string) => void;
  openChangeBlockGroupModal: (blockId: string) => void;
  openDeleteBlockModal: (blockId: string) => void;
  openCreateGroupModal: () => void;
  openRenameGroupModal: (groupId: string) => void;
  openDeleteGroupModal: (groupId: string) => void;
  openChangeGroupOrderModal: () => void;
  openChangeBlockOrderModal: (groupId: string) => void;
}

const ModalStore = createStore<ModalStateType>((set) => ({
  blockId: undefined,
  groupId: undefined,

  setBlockId: (blockId: string | undefined) => set({ blockId }),
  setGroupId: (groupId: string | undefined) => set({ groupId }),
}));

const ModalContext = createContext<ModalContextType | undefined>(undefined);

export const ModalProvider = ({
  children,
  blocks,
  blockGroups,
}: {
  children: ReactNode;
  blocks: Block[] | undefined;
  blockGroups: BlockGroup[] | undefined;
}) => {
  const { groupId, setBlockId, setGroupId } = useStore(
    ModalStore,
    useShallow((state) => state),
  );

  const resetModalIds = () => {
    setBlockId(undefined);
    setGroupId(undefined);
  };

  const socketListener = useSocketListeners();

  useEffect(() => {
    const onClose = (_: Event): void => {
      onCloseRenameBlockModal();
      onCloseChangeBlockGroupModal();
      onCloseDeleteBlockModal();
      onCloseCreateGroupModal();
      onCloseRenameGroupModal();
      onCloseDeleteGroupModal();
      onCloseChangeGroupOrderModal();
      onCloseChangeBlockOrderModal();
    };

    socketListener.addEventListener('close', onClose);

    return () => {
      socketListener.removeEventListener('close', onClose);
    };
  }, []);

  const {
    isOpen: isRenameBlockModalOpen,
    onOpen: onOpenRenameBlockModal,
    onOpenChange: onRenameBlockModalOpenChange,
    onClose: onCloseRenameBlockModal,
  } = useDisclosure();
  const {
    isOpen: isChangeBlockGroupModalOpen,
    onOpen: onOpenChangeBlockGroupModal,
    onOpenChange: onChangeBlockGroupModalOpenChange,
    onClose: onCloseChangeBlockGroupModal,
  } = useDisclosure();
  const {
    isOpen: isDeleteBlockModalOpen,
    onOpen: onOpenDeleteBlockModal,
    onOpenChange: onDeleteBlockModalOpenChange,
    onClose: onCloseDeleteBlockModal,
  } = useDisclosure();

  const {
    isOpen: isCreateGroupModalOpen,
    onOpen: onOpenCreateGroupModal,
    onOpenChange: onCreateGroupModalOpenChange,
    onClose: onCloseCreateGroupModal,
  } = useDisclosure();
  const {
    isOpen: isRenameGroupModalOpen,
    onOpen: onOpenRenameGroupModal,
    onOpenChange: onRenameGroupModalOpenChange,
    onClose: onCloseRenameGroupModal,
  } = useDisclosure();
  const {
    isOpen: isDeleteGroupModalOpen,
    onOpen: onOpenDeleteGroupModal,
    onOpenChange: onDeleteGroupModalOpenChange,
    onClose: onCloseDeleteGroupModal,
  } = useDisclosure();

  const {
    isOpen: isChangeGroupOrderModalOpen,
    onOpen: onOpenChangeGroupOrderModal,
    onOpenChange: onChangeGroupOrderModalOpenChange,
    onClose: onCloseChangeGroupOrderModal,
  } = useDisclosure();
  const {
    isOpen: isChangeBlockOrderModalOpen,
    onOpen: onOpenChangeBlockOrderModal,
    onOpenChange: onChangeBlockOrderModalOpenChange,
    onClose: onCloseChangeBlockOrderModal,
  } = useDisclosure();

  const openRenameBlockModal = (blockId: string) => {
    ModalStore.getState().setBlockId(blockId);
    onOpenRenameBlockModal();
  };

  const openChangeBlockGroupModal = (blockId: string) => {
    ModalStore.getState().setBlockId(blockId);
    onOpenChangeBlockGroupModal();
  };

  const openDeleteBlockModal = (blockId: string) => {
    ModalStore.getState().setBlockId(blockId);
    onOpenDeleteBlockModal();
  };

  const openCreateGroupModal = () => {
    onOpenCreateGroupModal();
  };

  const openRenameGroupModal = (groupId: string) => {
    ModalStore.getState().setGroupId(groupId);
    onOpenRenameGroupModal();
  };

  const openDeleteGroupModal = (groupId: string) => {
    ModalStore.getState().setGroupId(groupId);
    onOpenDeleteGroupModal();
  };

  const openChangeGroupOrderModal = () => {
    onOpenChangeGroupOrderModal();
  };

  const openChangeBlockOrderModal = (groupId: string) => {
    ModalStore.getState().setGroupId(groupId);
    onOpenChangeBlockOrderModal();
  };

  return (
    <ModalContext.Provider
      value={{
        openRenameBlockModal,
        openChangeBlockGroupModal,
        openDeleteBlockModal,
        openCreateGroupModal,
        openRenameGroupModal,
        openDeleteGroupModal,
        openChangeGroupOrderModal,
        openChangeBlockOrderModal,
      }}
    >
      {children}

      <RenameBlockModal
        isOpen={isRenameBlockModalOpen}
        onChange={() => {
          onRenameBlockModalOpenChange();
          resetModalIds();
        }}
      />

      <ChangeBlockGroupModal
        groups={blockGroups}
        isOpen={isChangeBlockGroupModalOpen}
        onChange={() => {
          onChangeBlockGroupModalOpenChange();
          resetModalIds();
        }}
      />

      <DeleteBlockModal
        isOpen={isDeleteBlockModalOpen}
        onChange={() => {
          onDeleteBlockModalOpenChange();
          resetModalIds();
        }}
      />

      <CreateGroupModal isOpen={isCreateGroupModalOpen} onChange={onCreateGroupModalOpenChange} />

      <RenameGroupModal
        isOpen={isRenameGroupModalOpen}
        onChange={() => {
          onRenameGroupModalOpenChange();
          resetModalIds();
        }}
      />

      <DeleteGroupModal
        isOpen={isDeleteGroupModalOpen}
        onChange={() => {
          onDeleteGroupModalOpenChange();
          resetModalIds();
        }}
      />

      <ChangeGroupOrderModal
        groups={blockGroups}
        isOpen={isChangeGroupOrderModalOpen}
        onChange={onChangeGroupOrderModalOpenChange}
      />

      <ChangeBlockOrderModal
        blocks={
          blockGroups
            ?.find((group) => groupId === group.groupId)
            ?.blockIds.map((id) => blocks?.find((block) => id === block.blockId))
            .filter(Boolean) as Block[]
        }
        isOpen={isChangeBlockOrderModalOpen}
        onChange={() => {
          onChangeBlockOrderModalOpenChange();
          resetModalIds();
        }}
      />
    </ModalContext.Provider>
  );
};

export const useModals = () => {
  const context = useContext(ModalContext);

  if (!context) {
    throw new Error('useModals must be used within a ModalProvider');
  }

  return context;
};

export const useModalStore = () => {
  const context = useContext(ModalContext);

  if (!context) {
    throw new Error('useModalStore must be used within a ModalProvider');
  }

  return useStore(
    ModalStore,
    useShallow((state) => state),
  );
};
