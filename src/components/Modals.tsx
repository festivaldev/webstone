import type SocketClient from '@/utilities/SocketClient';
import { useDisclosure } from '@nextui-org/react';
import React from 'react';
import {
  ChangeBlockGroupModal,
  ChangeBlockOrderModal,
  ChangeGroupOrderModal,
  CreateGroupModal,
  DeleteBlockModal,
  DeleteGroupModal,
  RenameBlockModal,
  RenameGroupModal,
} from './modal';

export type ModalHandler = {
  openRenameBlockModal: (blockId: string) => void;
  openChangeBlockGroupModal: (blockId: string) => void;
  openDeleteBlockModal: (blockId: string) => void;
  openCreateGroupModal: () => void;
  openRenameGroupModal: (groupId: string) => void;
  openDeleteGroupModal: (groupId: string) => void;
  openChangeGroupOrderModal: () => void;
  openChangeBlockOrderModal: (groupId: string) => void;
};

export type ModalProps = {
  socket?: SocketClient;
  blockGroups: BlockGroup[];
  blocks: Block[];
};

const Modals = React.forwardRef<ModalHandler, ModalProps>(({ socket, blockGroups, blocks }, ref) => {
  const [groupModalId, setGroupModalId] = React.useState<string>();
  const [blockModalId, setBlockModalId] = React.useState<string>();

  const {
    isOpen: isRenameBlockModalOpen,
    onOpen: _openRenameBlockModal,
    onOpenChange: onRenameBlockModalOpenChange,
  } = useDisclosure();
  const {
    isOpen: isChangeBlockGroupModalOpen,
    onOpen: _openChangeBlockGroupModal,
    onOpenChange: onChangeBlockGroupModalOpenChange,
  } = useDisclosure();
  const {
    isOpen: isDeleteBlockModalOpen,
    onOpen: _openDeleteBlockModal,
    onOpenChange: onDeleteBlockModalOpenChange,
  } = useDisclosure();

  const {
    isOpen: isCreateGroupModalOpen,
    onOpen: _openCreateGroupModal,
    onOpenChange: onCreateGroupModalOpenChange,
  } = useDisclosure();
  const {
    isOpen: isRenameGroupModalOpen,
    onOpen: _openRenameGroupModal,
    onOpenChange: onRenameGroupModalOpenChange,
  } = useDisclosure();
  const {
    isOpen: isDeleteGroupModalOpen,
    onOpen: _openDeleteGroupModal,
    onOpenChange: onDeleteGroupModalOpenChange,
  } = useDisclosure();

  const {
    isOpen: isChangeGroupOrderModalOpen,
    onOpen: _openChangeGroupOrderModal,
    onOpenChange: onChangeGroupOrderModalOpenChange,
  } = useDisclosure();
  const {
    isOpen: isChangeBlockOrderModalOpen,
    onOpen: _openChangeBlockOrderModal,
    onOpenChange: onChangeBlockOrderModalOpenChange,
  } = useDisclosure();

  React.useImperativeHandle(ref, () => ({
    openRenameBlockModal(blockId: string) {
      setBlockModalId(() => blockId);
      _openRenameBlockModal();
    },
    openChangeBlockGroupModal(blockId: string) {
      setBlockModalId(() => blockId);
      _openChangeBlockGroupModal();
    },
    openDeleteBlockModal(blockId: string) {
      setBlockModalId(() => blockId);
      _openDeleteBlockModal();
    },
    openCreateGroupModal() {
      _openCreateGroupModal();
    },
    openRenameGroupModal(groupId: string) {
      setGroupModalId(() => groupId);
      _openRenameGroupModal();
    },
    openDeleteGroupModal(groupId: string) {
      setGroupModalId(() => groupId);
      _openDeleteGroupModal();
    },
    openChangeGroupOrderModal() {
      _openChangeGroupOrderModal();
    },
    openChangeBlockOrderModal(groupId: string) {
      setGroupModalId(() => groupId);
      _openChangeBlockOrderModal();
    },
  }));

  const resetModalIds = () => {
    setGroupModalId(() => undefined);
    setBlockModalId(() => undefined);
  };

  return (
    <>
      <RenameBlockModal
        blockId={blockModalId}
        isOpen={isRenameBlockModalOpen}
        onChange={() => {
          onRenameBlockModalOpenChange();
          resetModalIds();
        }}
        onSubmit={(blockId, name) => {
          socket?.renameBlock(blockId, name);
        }}
      />

      <ChangeBlockGroupModal
        groups={blockGroups}
        blockId={blockModalId}
        groupId={blocks?.find((block) => block.blockId === blockModalId)?.groupId!}
        isOpen={isChangeBlockGroupModalOpen}
        onChange={() => {
          onChangeBlockGroupModalOpenChange();
          resetModalIds();
        }}
        onSubmit={(blockId, blockGroup) => {
          socket?.changeBlockGroup(blockId, blockGroup);
        }}
      />

      <DeleteBlockModal
        blockId={blockModalId}
        isOpen={isDeleteBlockModalOpen}
        onChange={() => {
          onDeleteBlockModalOpenChange();
          resetModalIds();
        }}
        onSubmit={(blockId) => {
          socket?.deleteBlock(blockId);
        }}
      />

      <CreateGroupModal
        isOpen={isCreateGroupModalOpen}
        onChange={onCreateGroupModalOpenChange}
        onSubmit={(name) => {
          socket?.createGroup(name);
        }}
      />

      <RenameGroupModal
        groupId={groupModalId}
        isOpen={isRenameGroupModalOpen}
        onChange={() => {
          onRenameGroupModalOpenChange();
          resetModalIds();
        }}
        onSubmit={(groupId, name) => {
          socket?.renameGroup(groupId, name);
        }}
      />

      <DeleteGroupModal
        groupId={groupModalId}
        isOpen={isDeleteGroupModalOpen}
        onChange={() => {
          onDeleteGroupModalOpenChange();
          resetModalIds();
        }}
        onSubmit={(groupId) => {
          socket?.deleteGroup(groupId);
        }}
      />

      <ChangeGroupOrderModal
        groups={blockGroups}
        isOpen={isChangeGroupOrderModalOpen}
        onChange={onChangeGroupOrderModalOpenChange}
        onOrderChanged={(groupId, newIndex) => {
          socket?.changeGroupIndex(groupId, newIndex);
        }}
      />

      <ChangeBlockOrderModal
        blocks={
          blockGroups
            .find((group) => groupModalId === group.groupId)
            ?.blockIds.map((id) => blocks?.find((block) => id === block.blockId))
            .filter(Boolean) as Block[]
        }
        isOpen={isChangeBlockOrderModalOpen}
        onChange={() => {
          onChangeBlockOrderModalOpenChange();
          resetModalIds();
        }}
        onOrderChanged={(blockId, newIndex) => {
          socket?.changeBlockIndex(blockId, newIndex);
        }}
      />
    </>
  );
});

export default Modals;
