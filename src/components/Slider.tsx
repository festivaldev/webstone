import { extendVariants, Slider } from '@nextui-org/react';

export default extendVariants(Slider, {
  variants: {
    color: {
      custom: {
        filler: 'bg-default-400',
        track: 'border-s-default-400',
        thumb: 'bg-default-400',
      },
    },
  },
});
