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

const ChangeGroupOrderModal = ({
  groups = [],
  isOpen,
  onChange,
}: {
  groups?: BlockGroup[];
} & UseDisclosureProps): React.ReactNode => {
  const { changeGroupIndex } = useSocket();
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
            <ModalHeader className="flex flex-col gap-1">Reorder Groups</ModalHeader>
            <ModalBody>
              <DraggableList
                items={groups}
                template={(item) => <p>{item.name}</p>}
                onChange={(_, item, index) => {
                  changeGroupIndex(item.groupId, index);
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

export default ChangeGroupOrderModal;
