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

const RenameBlockModal = ({
  blockId,
  onSubmit,
  isOpen,
  onChange,
}: {
  blockId?: string;
  onSubmit?: (blockId: string, name: string) => void;
} & UseDisclosureProps): React.ReactNode => {
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
            <ModalHeader className="flex flex-col gap-1">Rename Block</ModalHeader>
            <ModalBody>
              <p>Rename this block to:</p>
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

                  onSubmit?.(blockId!, value);
                }}
                isDisabled={!value?.length || value?.length > 64}
              >
                Rename
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default RenameBlockModal;
