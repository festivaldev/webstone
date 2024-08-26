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

const DeleteBlockModal = ({
  blockId,
  onSubmit,
  isOpen,
  onChange,
}: {
  blockId?: string;
  onSubmit?: (blockId: string) => void;
} & UseDisclosureProps): React.ReactNode => (
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
          <ModalHeader className="flex flex-col gap-1">Delete Block</ModalHeader>
          <ModalBody>
            <p>Do you really want to delete this block?</p>
          </ModalBody>
          <ModalFooter>
            <Button color="default" variant="flat" onPress={onClose}>
              Cancel
            </Button>
            <Button
              color="danger"
              onPress={() => {
                onSubmit?.(blockId!);
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

export default DeleteBlockModal;
