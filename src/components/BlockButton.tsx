import classNames from '@/utilities/classNames';
import { PencilIcon, TrashIcon } from '@heroicons/react/24/outline';
import { Button, Card, CardBody, CardFooter, CardHeader, Slider, Tooltip } from '@nextui-org/react';

const BlockButton = ({
  block,
  setBlockState,
  setBlockPower,
  renameBlock,
  unregisterBlock,
}: {
  block: Block;
  setBlockState: (blockId: string, powered: boolean) => void;
  setBlockPower: (blockId: string, power: number) => void;
  renameBlock: (blockId: string) => void;
  unregisterBlock: (blockId: string) => void;
}): React.ReactNode => (
  <Button
    as="div"
    key={block.blockId}
    className={classNames('aspect-square h-auto px-0', {
      'bg-green-600': block.powered,
    })}
    onClick={() => setBlockState(block.blockId, !block.powered)}
  >
    <Card className="h-full w-full bg-transparent">
      <CardHeader className="flex justify-end gap-0.5 md:gap-1">
        <Tooltip showArrow placement="bottom" closeDelay={0} content="Rename">
          <Button
            className="size-8 min-w-0 md:size-10"
            isIconOnly
            variant="light"
            onClick={(e) => {
              e.stopPropagation();
              setTimeout(() => {
                renameBlock(block.blockId);
              });
            }}
          >
            <PencilIcon className="size-4 md:size-5" />
          </Button>
        </Tooltip>

        <Tooltip showArrow placement="bottom" closeDelay={0} content="Delete">
          <Button
            className="size-8 min-w-0 md:size-10"
            isIconOnly
            variant="light"
            onClick={(e) => {
              e.stopPropagation();
              setTimeout(() => {
                unregisterBlock(block.blockId);
              });
            }}
          >
            <TrashIcon className="size-4 md:size-5" />
          </Button>
        </Tooltip>
      </CardHeader>

      <CardBody className="flex justify-center text-center">
        <Tooltip showArrow placement="bottom" closeDelay={0} content={block.name} hidden={block.name.length < 20}>
          <span className="overflow-hidden overflow-ellipsis text-xl font-semibold">{block.name}</span>
        </Tooltip>
      </CardBody>

      <CardFooter>
        <Slider
          color="danger"
          size="md"
          step={1}
          label="Strength"
          maxValue={15}
          minValue={0}
          defaultValue={15}
          value={block.power}
          onChange={(value) => setBlockPower(block.blockId, value as number)}
        />
      </CardFooter>
    </Card>
  </Button>
);

export default BlockButton;
