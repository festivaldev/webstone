import { ModalMotionProps } from '@/utilities/motionProps';
import {
  Button,
  Input,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  UseDisclosureProps,
} from '@nextui-org/react';
import React from 'react';
import { useSocket } from '../SocketProvider';

const CreateGroupModal = ({ isOpen, onChange }: UseDisclosureProps): React.ReactNode => {
  const { createGroup } = useSocket();

  const [value, setValue] = React.useState<string>('');

  return (
    <Modal
      isDismissable={false}
      isOpen={isOpen}
      onOpenChange={() => {
        setValue(() => '');
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
            <ModalHeader className="flex flex-col gap-1">Create Group</ModalHeader>
            <ModalBody>
              <p>Enter a name for the new group:</p>
              <Input autoFocus value={value} onChange={(e) => setValue(() => e.target.value)} variant="faded" />
            </ModalBody>
            <ModalFooter>
              <Button
                color="default"
                variant="flat"
                onPress={() => {
                  onClose();
                  setValue(() => '');
                }}
              >
                Cancel
              </Button>
              <Button
                color="primary"
                onPress={() => {
                  onClose();
                  setValue(() => '');

                  createGroup(value);
                }}
                isDisabled={!value?.length || value?.length > 64}
              >
                Create
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default CreateGroupModal;
