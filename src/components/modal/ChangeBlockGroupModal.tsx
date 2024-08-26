import { ModalMotionProps } from '@/utilities/motionProps';
import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Radio,
  RadioGroup,
  UseDisclosureProps,
} from '@nextui-org/react';
import React from 'react';

const ChangeBlockGroupModal = ({
  groups = [],
  blockId,
  groupId,
  onSubmit,
  isOpen,
  onChange,
}: {
  groups?: BlockGroup[];
  blockId?: string;
  groupId?: string;
  onSubmit?: (blockId: string, groupId: string) => void;
} & UseDisclosureProps): React.ReactNode => {
  const [value, setValue] = React.useState<string>('');

  React.useEffect(() => {
    setValue(() => groupId || '');
  }, [groupId]);

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
            <ModalHeader className="flex flex-col gap-1">Change Group</ModalHeader>
            <ModalBody>
              <RadioGroup label="Select a group to assign this block to:" value={value} onValueChange={setValue}>
                <Radio value="">No group</Radio>

                {groups.map((group) => (
                  <Radio key={group.groupId} value={group.groupId}>
                    {group.name}
                  </Radio>
                ))}
              </RadioGroup>
            </ModalBody>
            <ModalFooter>
              <Button color="default" variant="flat" onPress={onClose}>
                Cancel
              </Button>
              <Button
                color="primary"
                isDisabled={value === groupId || (value === '' && groupId === null)}
                onPress={() => {
                  onSubmit?.(blockId!, value);
                  onClose();
                }}
              >
                Confirm
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default ChangeBlockGroupModal;
