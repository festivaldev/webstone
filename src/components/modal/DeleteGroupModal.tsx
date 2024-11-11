import { ModalMotionProps } from '@/utilities/motionProps';
import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  UseDisclosureProps,
} from '@nextui-org/react';
import { useSocket } from '../SocketProvider';
import { useModalStore } from './ModalProvider';

const DeleteGroupModal = ({ isOpen, onChange }: UseDisclosureProps): React.ReactNode => {
  const { groupId } = useModalStore();
  const { deleteGroup } = useSocket();

  return (
    <Modal
      isDismissable={false}
      isOpen={isOpen}
      onOpenChange={() => {
        onChange?.(isOpen);
      }}
      placement="center"
      motionProps={ModalMotionProps}
      classNames={{
        base: 'mx-6',
      }}
    >
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader className="flex flex-col gap-1">Delete Group</ModalHeader>
            <ModalBody>
              <p>
                Do you really want to delete this group?
                <br />
                Blocks assigned to this group will not be deleted.
              </p>
            </ModalBody>
            <ModalFooter>
              <Button color="default" variant="flat" onPress={onClose}>
                Cancel
              </Button>
              <Button
                color="danger"
                onPress={() => {
                  deleteGroup(groupId!);
                  onClose();
                }}
              >
                Delete
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default DeleteGroupModal;
