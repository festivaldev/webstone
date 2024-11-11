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
import React from 'react';
import DraggableList from '../DraggableList';
import { useSocket } from '../SocketProvider';

const ChangeBlockOrderModal = ({
  blocks = [],
  isOpen,
  onChange,
}: {
  blocks?: Block[];
} & UseDisclosureProps): React.ReactNode => {
  const { changeBlockIndex } = useSocket();

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
            <ModalHeader className="flex flex-col gap-1">Reorder Blocks</ModalHeader>
            <ModalBody>
              <DraggableList
                items={blocks}
                template={(item) => <p>{item.name}</p>}
                onChange={(_, item, index) => {
                  changeBlockIndex(item.blockId, index);
                }}
              />
            </ModalBody>
            <ModalFooter>
              <Button color="primary" onPress={onClose}>
                Done
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default ChangeBlockOrderModal;
