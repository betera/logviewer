package com.betera.logviewer.ui.veto;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class VetoableListSelectionModel
        implements ListSelectionModel
{

    private ListSelectionModel delegate;
    private Vetoable veto;

    public VetoableListSelectionModel(ListSelectionModel delegate, Vetoable veto)
    {
        this.delegate = delegate;
        this.veto = veto;
    }

    @Override
    public void setSelectionInterval(int index0, int index1)
    {
        if ( !veto.veto() )
        {
            delegate.setSelectionInterval(index0, index1);
        }
    }

    @Override
    public void addSelectionInterval(int index0, int index1)
    {
        if ( !veto.veto() )
        {
            delegate.addSelectionInterval(index0, index1);
        }
    }

    @Override
    public void removeSelectionInterval(int index0, int index1)
    {
        if ( !veto.veto() )
        {
            delegate.removeSelectionInterval(index0, index1);
        }
    }

    @Override
    public int getMinSelectionIndex()
    {
        return delegate.getMinSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex()
    {
        return delegate.getMaxSelectionIndex();
    }

    @Override
    public boolean isSelectedIndex(int index)
    {
        return delegate.isSelectedIndex(index);
    }

    @Override
    public int getAnchorSelectionIndex()
    {
        return delegate.getAnchorSelectionIndex();
    }

    @Override
    public void setAnchorSelectionIndex(int index)
    {
        if ( !veto.veto() )
        {
            delegate.setAnchorSelectionIndex(index);
        }
    }

    @Override
    public int getLeadSelectionIndex()
    {
        return delegate.getLeadSelectionIndex();
    }

    @Override
    public void setLeadSelectionIndex(int index)
    {
        if ( !veto.veto() )
        {
            delegate.setLeadSelectionIndex(index);
        }
    }

    @Override
    public void clearSelection()
    {
        if ( !veto.veto() )
        {
            delegate.clearSelection();
        }
    }

    @Override
    public boolean isSelectionEmpty()
    {
        return delegate.isSelectionEmpty();
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before)
    {
        if ( !veto.veto() )
        {
            delegate.insertIndexInterval(index, length, before);
        }
    }

    @Override
    public void removeIndexInterval(int index0, int index1)
    {
        if ( !veto.veto() )
        {
            delegate.removeIndexInterval(index0, index1);
        }
    }

    @Override
    public boolean getValueIsAdjusting()
    {
        return delegate.getValueIsAdjusting();
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting)
    {
        delegate.setValueIsAdjusting(valueIsAdjusting);

    }

    @Override
    public int getSelectionMode()
    {
        return delegate.getSelectionMode();
    }

    @Override
    public void setSelectionMode(int selectionMode)
    {
        delegate.setSelectionMode(selectionMode);
    }

    @Override
    public void addListSelectionListener(ListSelectionListener x)
    {
        delegate.addListSelectionListener(x);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener x)
    {
        delegate.removeListSelectionListener(x);
    }
}
