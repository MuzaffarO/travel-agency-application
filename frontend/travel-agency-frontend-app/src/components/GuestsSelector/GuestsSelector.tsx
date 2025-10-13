import Icon from "../../ui/Icon";
import {useAppDispatch} from "../../store/hooks/useAppDispatch.ts";
import {useAppSelector} from "../../store/hooks/useAppSelector.ts";
import {
  decrementAdults,
  decrementChildren,
  incrementAdults,
  incrementChildren
} from "../../store/guests/guestsSlice.ts";

const GuestsSelector = () => {
  const dispatch = useAppDispatch();

  const {adults, children} = useAppSelector(state => state.guests);

  const handleRemoveAdult = () => {
    dispatch(decrementAdults());
  };

  const handleAddAdult = () => {
    dispatch(incrementAdults());
  };

  const handleRemoveChild = () => {
    dispatch(decrementChildren());
  };

  const handleAddChild = () => {
    dispatch(incrementChildren());
  };

  return (
    <div className='p-4 max-w-52 border border-grey-05 rounded-md shadow-card flex flex-col gap-2 bg-white
      absolute top-15 z-10 min-w-full
    '>
      <div className='flex items-center justify-between'>
        <p className='text-sm'>Adults</p>
        <div className='flex items-center gap-2.5'>
          <button
            type='button'
            onClick={handleRemoveAdult}
            className='border border-grey-06 rounded-full w-8 h-8'
          >
            <Icon name='icon-minus' width={28} height={28} />
          </button>
          <p>{adults}</p>
          <button
            type='button'
            onClick={handleAddAdult}
            className='border border-blue-05 rounded-full w-8 h-8'
          >
            <Icon name='icon-plus' width={28} height={28} className='text-blue-05'/>
          </button>
        </div>
      </div>

      <div className='flex items-center justify-between'>
        <p className='text-sm'>Children</p>
        <div className='flex items-center gap-2.5'>
          <button
            type='button'
            onClick={handleRemoveChild}
            className='border border-grey-06 rounded-full w-8 h-8'
          >
            <Icon name='icon-minus' width={28} height={28}/>
          </button>
          <p>{children}</p>
          <button
            type='button'
            onClick={handleAddChild}
            className='border border-blue-05 rounded-full w-8 h-8'
          >
            <Icon name='icon-plus' width={28} height={28} className='text-blue-05'/>
          </button>
        </div>
      </div>
    </div>
  )
}

export default GuestsSelector;